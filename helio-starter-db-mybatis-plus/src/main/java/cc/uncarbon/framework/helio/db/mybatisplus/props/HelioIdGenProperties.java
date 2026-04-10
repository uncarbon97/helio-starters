package cc.uncarbon.framework.helio.db.mybatisplus.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Helio ID 生成器配置属性类
 *
 * @author Uncarbon
 */
@ConfigurationProperties(prefix = "helio.db.idgen")
@Data
public class HelioIdGenProperties {

    /**
     * 雪花 ID
     */
    private Snowflake snowflake;

    @Data
    public static final class Snowflake {

        /**
         * 数据中心ID
         */
        private Long datacenterId;

        /**
         * 起始日期
         */
        private String epochDate;

    }
}
