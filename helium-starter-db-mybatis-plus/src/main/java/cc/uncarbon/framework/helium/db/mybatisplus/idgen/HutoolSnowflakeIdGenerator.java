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

        long ipHash;
        try {
            // 当前机器的局域网IP
            ipHash = NetUtil.ipv4ToLong(NetUtil.getLocalhostStr());
        } catch (Exception e) {
            ipHash = NetUtil.getLocalhost().hashCode();
        }
        /*
        datacenterId 由配置项显式指定（机房维度，需真实区分机房），未配置默认 0（单机房）
        越界直接失败：静默 floorMod 回绕会与其他节点撞 ID
         */
        int datacenterId = subProps.getDatacenterId() == null ? 0 : subProps.getDatacenterId();
        if (datacenterId < 0 || datacenterId > 31) {
            throw new IllegalArgumentException(
                    LOG_PREFIX + " datacenterId 须在 [0, 31] 区间，当前: " + datacenterId);
        }

        // Hutool 的雪花生成器仅支持 [0, 31]；floorMod 保证非负
        int workerId = Math.floorMod(ipHash, 32);

        String epochDateStr = subProps.getEpochDate();
        if (epochDateStr == null || epochDateStr.isBlank()) {
            throw new IllegalArgumentException(LOG_PREFIX + " epochDate 不能为空，格式应为 yyyy-MM-dd");
        }
        Date epochDate;
        try {
            epochDate = DateUtil.parseDate(subProps.getEpochDate());
        } catch (Exception e) {
            throw new IllegalArgumentException(LOG_PREFIX + " epochDate 格式不合法，应为 yyyy-MM-dd: " + epochDateStr);
        }
        if (epochDate.after(new Date())) {
            // 纪元晚于当前时间会生成负数雪花 ID，落 UNSIGNED 主键直接报错
            throw new IllegalArgumentException(LOG_PREFIX + " epochDate 不能晚于当前时间: " + epochDateStr);
        }

        log.info(LOG_PREFIX + " >> workerId=[{}], datacenterId=[{}], epochDate=[{}]",
                workerId, datacenterId, epochDateStr);
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
