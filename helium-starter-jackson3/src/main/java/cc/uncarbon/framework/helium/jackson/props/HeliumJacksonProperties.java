package cc.uncarbon.framework.helium.jackson.props;

import cc.uncarbon.framework.helium.base.constant.ConfigurationPropertiesPrefix;
import cc.uncarbon.framework.helium.jackson.module.BaseEnumFormatModule;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Helium 集成 Jackson 配置属性类
 *
 * @author Uncarbon
 */
@ConfigurationProperties(prefix = ConfigurationPropertiesPrefix.JACKSON)
@Data
public class HeliumJacksonProperties {

    /**
     * 主要调整 {@link BaseEnumFormatModule} 的行为
     */
    @NestedConfigurationProperty
    private BaseEnumConfig baseEnum = new BaseEnumConfig();

}
