package cc.uncarbon.framework.helio.jackson.props;

import cc.uncarbon.framework.helio.base.constant.ConfigurationPropertiesPrefix;
import cc.uncarbon.framework.helio.jackson.module.BaseEnumFormatModule;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Helio 集成 Jackson 配置属性类
 *
 * @author Uncarbon
 */
@ConfigurationProperties(prefix = ConfigurationPropertiesPrefix.JACKSON)
@Data
public class HelioJacksonProperties {

    /**
     * 主要调整 {@link BaseEnumFormatModule} 的行为
     */
    @NestedConfigurationProperty
    private BaseEnumConfig baseEnum;

}
