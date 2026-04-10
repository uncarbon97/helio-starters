package cc.uncarbon.framework.helio.jackson.props;

import cc.uncarbon.framework.helio.jackson.module.BaseEnumFormatModule;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(prefix = "helio.jackson")
@Data
public class HelioJacksonProperties {

    /**
     * 主要调整 {@link BaseEnumFormatModule} 的行为
     */
    @NestedConfigurationProperty
    private BaseEnumConfig baseEnum;

}
