package cc.uncarbon.framework.helium.db.mybatisplus.props;

import cc.uncarbon.framework.helium.base.constant.ConfigurationPropertiesPrefix;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Helium ID 生成器配置属性类
 *
 * @author Uncarbon
 */
@ConfigurationProperties(prefix = ConfigurationPropertiesPrefix.DB_IDGEN)
@Data
public class HeliumIdGenProperties {

    /**
     * 雪花 ID（默认实例，避免未配置时 NPE）
     */
    private Snowflake snowflake = new Snowflake();

    @Data
    public static final class Snowflake {

        /**
         * 数据中心ID
         */
        private Integer datacenterId;

        /**
         * 起始日期
         */
        private String epochDate;

    }
}
