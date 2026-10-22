package cc.uncarbon.framework.helium.db.fieldcrypto.reconciler;

import cc.uncarbon.framework.helium.db.fieldcrypto.blindindex.BlindIndex;
import cc.uncarbon.framework.helium.db.fieldcrypto.constant.FieldCryptoConstant;
import cc.uncarbon.framework.helium.db.fieldcrypto.field.FieldCipher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 根密钥双指纹 canary 启动比对
 *
 * <p>框架维护两组固定测试向量的指纹，存业务库元数据表 {@code helium_field_crypto_canary}（两行）：</p>
 * <ul>
 *   <li>{@code field-ik}：indexRootKey 指纹——索引值无代际标记，漂移 = 多节点静默混写两代索引</li>
 *   <li>{@code kek-data}：dataRootKey 指纹——密文前缀只路由 keyId 不验根，漂移 = 同 keyId 静默写坏密文
 *       （启动期零信号、纯静默数据损坏，后果重于 IK 漂移）</li>
 * </ul>
 *
 * <p>首启幂等写入（主键冲突即跳过，防多节点并发首启竞态）；此后每次启动逐一比对——
 * <b>任一</b>不匹配 = 对应根密钥代际漂移（多节点配置不一致 / 漏改 env / 轮换窗口混写）→ 拒启并告警。</p>
 *
 * <p>轮换覆写规程：不走常规启动路径——由 runbook 专用维护开关
 * {@code helium.db.field-crypto.canary.allow-overwrite=true}（单次生效、写操作审计日志）允许首节点覆写新指纹，
 * 其余节点随后正常比对通过；覆写资格用尽后本实例内再次不匹配一律拒启，防开关常开沦为漂移绕过手段。</p>
 *
 * <p>非业务表，不进租户路由；DDL 由业务侧升级脚本携带（MySQL/PG 双份）。</p>
 *
 * @author Uncarbon
 */
@Slf4j
@RequiredArgsConstructor
public class FieldCryptoCanaryChecker implements SmartInitializingSingleton {

    public static final String TABLE_NAME = "sys_field_crypto_canary";
    public static final String PURPOSE_FIELD_IK = "field-ik";
    public static final String PURPOSE_KEK_DATA = "kek-data";

    private static final String SELECT_SQL = "SELECT fingerprint FROM " + TABLE_NAME + " WHERE purpose = ?";
    private static final String INSERT_SQL = "INSERT INTO " + TABLE_NAME + " (purpose, fingerprint) VALUES (?, ?)";
    private static final String UPDATE_SQL = "UPDATE " + TABLE_NAME + " SET fingerprint = ? WHERE purpose = ?";

    private final JdbcTemplate jdbcTemplate;
    private final FieldCipher fieldCipher;
    private final BlindIndex blindIndex;
    private final boolean enabled;
    private final boolean allowOverwrite;

    /**
     * 覆写资格：单次生效（无锁并发场景下同实例最多消耗一次）
     */
    private final AtomicBoolean overwriteGranted = new AtomicBoolean(allowOverwrite);

    private static IllegalStateException drift(String purpose) {
        return new IllegalStateException(
                FieldCryptoConstant.LOG_PREFIX + " 根密钥 canary 指纹不匹配（purpose=" + purpose + "），"
                        + "请排查是否存在：多节点根密钥配置不一致 / env 漏改 / 轮换窗口混写等情况；"
                        + "目前拒绝启动以防静默写坏数据，受控轮换请打开 canary.allow-overwrite 维护开关");
    }

    @Override
    public void afterSingletonsInstantiated() {
        if (!enabled) {
            return;
        }

        ensureTableExists();
        checkOne(PURPOSE_FIELD_IK, blindIndex.indexRootKeyCanaryFingerprint());
        checkOne(PURPOSE_KEK_DATA, fieldCipher.dataRootKeyCanaryFingerprint());
        log.info(FieldCryptoConstant.LOG_PREFIX + " >> 根密钥双指纹 canary 比对通过");
    }

    private void ensureTableExists() {
        try {
            jdbcTemplate.execute("SELECT 1 FROM " + TABLE_NAME + " LIMIT 1");
        } catch (Exception e) {
            throw new IllegalStateException(
                    FieldCryptoConstant.LOG_PREFIX + " 元数据表 " + TABLE_NAME + " 不存在或不可达", e);
        }
    }

    private void checkOne(String purpose, String expectedFingerprint) {
        String existing;
        try {
            existing = jdbcTemplate.query(SELECT_SQL, rs -> rs.next() ? rs.getString(1) : null, purpose);
        } catch (Exception e) {
            throw new IllegalStateException(FieldCryptoConstant.LOG_PREFIX + " canary 指纹读取失败: purpose=" + purpose, e);
        }

        if (existing == null || existing.isBlank()) {
            try {
                jdbcTemplate.update(INSERT_SQL, purpose, expectedFingerprint);
                log.info(FieldCryptoConstant.LOG_PREFIX + " >> canary 首次启动，写入 {} 对应指纹", purpose);
            } catch (Exception race) {
                // 多节点并发首启竞态：主键冲突 → 重新读取比对
                String winner = jdbcTemplate.query(SELECT_SQL, rs -> rs.next() ? rs.getString(1) : null, purpose);
                if (winner == null || !winner.equals(expectedFingerprint)) {
                    throw drift(purpose);
                }
            }
            return;
        }

        if (existing.equals(expectedFingerprint)) {
            return;
        }

        if (overwriteGranted.compareAndSet(true, false)) {
            // 单次生效的维护开关：仅轮换时允许
            log.warn(FieldCryptoConstant.LOG_PREFIX + " >> [审计] canary 维护开关生效：覆写指纹 purpose={}（旧值不匹配）。"
                    + "本开关为单次生效，请立即确认这是一次受控轮换操作并回置 canary.allow-overwrite=false", purpose);
            jdbcTemplate.update(UPDATE_SQL, expectedFingerprint, purpose);
            return;
        }

        throw drift(purpose);
    }
}
