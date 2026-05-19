package cc.uncarbon.framework.helio.openapi.props;


import cc.uncarbon.framework.helio.base.constant.ConfigurationPropertiesPrefix;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Helio 集成 OpenAPI 3 配置属性类
 *
 * @author Uncarbon
 */
@ConfigurationProperties(prefix = ConfigurationPropertiesPrefix.OPENAPI3)
@Data
public class HelioOpenApi3Properties {


    private Nextdoc4j nextdoc4j;

    @Data
    public static final class Nextdoc4j {

        /**
         * 是否启用
         */
        private Boolean enabled;

    }

}
