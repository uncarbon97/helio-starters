package cc.uncarbon.framework.crud.handler;

import cc.uncarbon.framework.core.enums.IdGeneratorStrategyEnum;
import cc.uncarbon.framework.core.props.HelioProperties;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Singleton;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * 自定义雪花ID生成器
 * @author Uncarbon
 */
@Slf4j
public class HelioIdentifierGeneratorHandler implements IdentifierGenerator {

    private Snowflake snowflakeInstance;


    public HelioIdentifierGeneratorHandler(HelioProperties helioProperties) {
        long workerId;

        try {
            // 当前机器的局域网IP
            workerId = NetUtil.ipv4ToLong(NetUtil.getLocalhostStr());
        } catch (Exception e) {
            workerId = NetUtil.getLocalhost().hashCode();
        }

        if (IdGeneratorStrategyEnum.SNOWFLAKE.equals(helioProperties.getCrud().getIdGenerator().getStrategy())) {
            // Hutool的雪花生成器仅支持0-31
            workerId = workerId % 32;
            Long datacenterId = helioProperties.getCrud().getIdGenerator().getDatacenterId();
            Date epochDate = DateUtil.parseDate(helioProperties.getCrud().getIdGenerator().getEpochDate());

            log.info("[主键ID生成器] >> strategy=[{}], workerId=[{}], datacenterId=[{}], epochDate=[{}]",
                    helioProperties.getCrud().getIdGenerator().getStrategy().getLabel(),
                    workerId,
                    datacenterId,
                    epochDate
            );

            snowflakeInstance = getSnowflake(workerId, datacenterId, epochDate);
        }
    }

    @Override
    public Number nextId(Object entity) {
        return snowflakeInstance.nextId();
    }

    @Override
    public String nextUUID(Object entity) {
        return IdUtil.randomUUID();
    }

    private static Snowflake getSnowflake(long workerId, long datacenterId, Date epochDate) {
        return Singleton.get(Snowflake.class, epochDate, workerId, datacenterId, false);
    }

}
