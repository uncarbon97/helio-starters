package cc.uncarbon.framework.helium.db.mybatisplus.idgen;

import cc.uncarbon.framework.helium.db.mybatisplus.props.HeliumIdGenProperties;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * 基于 hutool 的雪花 ID 生成器
 *
 * @author Uncarbon
 */
@Slf4j
public class HutoolSnowflakeIdGenerator implements IdentifierGenerator {

    private final Snowflake snowflakeInstance;
    private static final String LOG_PREFIX = "[Framework][雪花ID生成器]";

    public HutoolSnowflakeIdGenerator(HeliumIdGenProperties props) {
        var subProps = props.getSnowflake();
        if (subProps == null) {
            subProps = new HeliumIdGenProperties.Snowflake();
            log.info(LOG_PREFIX + " >> 未配置 helium.db-idgen.snowflake，使用默认配置");
        }

        long workerId;
        String workerIdSource;
        if (subProps.getWorkerId() != null) {
            workerId = subProps.getWorkerId();
            workerIdSource = "配置";
            if (workerId < 0 || workerId > 31) {
                throw new IllegalArgumentException(
                        LOG_PREFIX + " workerId 须在 [0, 31] 区间，当前: " + workerId);
            }
        } else {
            try {
                // 当前机器的局域网IP
                workerId = NetUtil.ipv4ToLong(NetUtil.getLocalhostStr());
            } catch (Exception e) {
                workerId = NetUtil.getLocalhost().hashCode();
            }
            // Hutool 的雪花生成器仅支持 0-31；floorMod 保证非负（IP hashCode 可能为负，% 会保留符号）
            workerId = Math.floorMod(workerId, 32);
            workerIdSource = "本机 IP 推导";
            log.warn(LOG_PREFIX + " >> workerId 未显式配置，按本机 IP mod 32 推导为 [{}]；"
                    + "多节点部署同网段 IP 极易碰撞导致重复主键，生产环境请显式配置 helium.db-idgen.snowflake.worker-id", workerId);
        }

        Integer datacenterId = subProps.getDatacenterId();
        if (datacenterId == null) {
            datacenterId = 0;
        }
        // 越界直接失败：静默 floorMod 回绕会与其他节点撞 ID
        if (datacenterId < 0 || datacenterId > 31) {
            throw new IllegalArgumentException(
                    LOG_PREFIX + " datacenterId 须在 [0, 31] 区间，当前: " + datacenterId);
        }

        String epochDateStr = subProps.getEpochDate();
        if (epochDateStr == null || epochDateStr.isBlank()) {
            epochDateStr = HeliumIdGenProperties.Snowflake.DEFAULT_EPOCH_DATE;
        }
        Date epochDate;
        try {
            epochDate = DateUtil.parseDate(epochDateStr);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    LOG_PREFIX + " epochDate 格式不合法（应为 yyyy-MM-dd）: " + epochDateStr, e);
        }
        if (epochDate.after(new Date())) {
            // 纪元晚于当前时间会生成负数雪花 ID，落 UNSIGNED 主键直接报错
            throw new IllegalArgumentException(
                    LOG_PREFIX + " epochDate 不能晚于当前时间: " + epochDateStr);
        }

        log.info(LOG_PREFIX + " >> workerId=[{}]（来源: {}）, datacenterId=[{}], epochDate=[{}]",
                workerId, workerIdSource, datacenterId, epochDateStr);
        snowflakeInstance = new Snowflake(epochDate, workerId, datacenterId, false);
    }

    @Override
    public Number nextId(Object entity) {
        return snowflakeInstance.nextId();
    }

    /**
     * 兼容 String 主键实体
     * 兼容但不推荐使用 String 作为主键，建议 Long 雪花 ID
     */
    @Override
    public String nextUUID(Object entity) {
        return IdUtil.fastSimpleUUID();
    }
}
