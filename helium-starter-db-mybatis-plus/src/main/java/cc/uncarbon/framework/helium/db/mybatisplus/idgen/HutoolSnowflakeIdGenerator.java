package cc.uncarbon.framework.helium.db.mybatisplus.idgen;

import cc.uncarbon.framework.helium.db.mybatisplus.props.HeliumIdGenProperties;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.net.NetUtil;
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

        try {
            // 当前机器的局域网IP
            workerId = NetUtil.ipv4ToLong(NetUtil.getLocalhostStr());
        } catch (Exception e) {
            workerId = NetUtil.getLocalhost().hashCode();
        }

        // Hutool 的雪花生成器仅支持 0-31；floorMod 保证非负（IP hashCode 可能为负，% 会保留符号）
        workerId = Math.floorMod(workerId, 32);

        Integer datacenterId = subProps.getDatacenterId();
        if (datacenterId == null) {
            datacenterId = 0;
        }
        // 同样钳制到 0-31，避免配置越界触发 hutool Assert 失败
        datacenterId = Math.floorMod(datacenterId, 32);

        String epochDateStr = subProps.getEpochDate();
        Date epochDate = DateUtil.parseDate(epochDateStr);

        log.info(LOG_PREFIX + " >> 根据本机 IP 计算的workerId=[{}], datacenterId=[{}], epochDate=[{}]",
                workerId, datacenterId, epochDate);
        snowflakeInstance = new Snowflake(epochDate, workerId, datacenterId, false);
    }

    @Override
    public Number nextId(Object entity) {
        return snowflakeInstance.nextId();
    }

    @Override
    public String nextUUID(Object entity) {
        throw new UnsupportedOperationException();
    }
}
