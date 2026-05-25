package cc.uncarbon.framework.helium.web.props;

import cc.uncarbon.framework.helium.base.constant.ConfigurationPropertiesPrefix;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Helium 增强 Web 配置属性类
 *
 * @author Uncarbon
 */
@ConfigurationProperties(prefix = ConfigurationPropertiesPrefix.WEB)
@Data
public class HeliumWebProperties {

}
