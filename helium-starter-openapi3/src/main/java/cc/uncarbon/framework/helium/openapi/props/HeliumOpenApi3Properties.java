package cc.uncarbon.framework.helium.openapi.props;


import cc.uncarbon.framework.helium.base.constant.ConfigurationPropertiesPrefix;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Helium 集成 OpenAPI 3 配置属性类
 *
 * @author Uncarbon
 */
@ConfigurationProperties(prefix = ConfigurationPropertiesPrefix.OPENAPI3)
@Data
public class HeliumOpenApi3Properties {


    private Nextdoc4j nextdoc4j;

    @Data
    public static final class Nextdoc4j {

        /**
         * 是否启用
         */
        private Boolean enabled;

    }

}
