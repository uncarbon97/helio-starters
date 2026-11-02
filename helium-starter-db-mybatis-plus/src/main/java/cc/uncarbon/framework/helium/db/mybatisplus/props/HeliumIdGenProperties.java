package cc.uncarbon.framework.helium.db.mybatisplus.props;

import cc.uncarbon.framework.helium.base.constant.ConfigurationPropertiesPrefix;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Helium ID 生成器配置属性类
 *
 * @author Uncarbon
 */
@Data
@ConfigurationProperties(prefix = ConfigurationPropertiesPrefix.DB_IDGEN)
public class HeliumIdGenProperties {

    /**
     * 雪花 ID（默认实例，避免未配置时 NPE）
     */
    private Snowflake snowflake = new Snowflake();

    @Data
    public static final class Snowflake {

        /**
         * 数据中心ID，范围 [0, 31]
         */
        private Integer datacenterId;

        /**
         * 工作节点ID，范围 [0, 31]；未配置时按本机 IP 推导（多节点同网段易碰撞，生产环境建议显式配置或通过环境变量注入）
         */
        private Integer workerId;

        /**
         * 起始日期（yyyy-MM-dd）；一经上线不可更改，否则新旧 ID 可能重叠
         * <p>默认值固定，未配置时也能启动；但生产环境仍建议显式写入配置以避免升级框架时误改</p>
         */
        private String epochDate = DEFAULT_EPOCH_DATE;

        public static final String DEFAULT_EPOCH_DATE = "2024-01-01";

    }
}
