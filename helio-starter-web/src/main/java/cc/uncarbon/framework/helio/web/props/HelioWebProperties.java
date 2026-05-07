package cc.uncarbon.framework.helio.web.props;

import cc.uncarbon.framework.helio.base.constant.ConfigurationPropertiesPrefix;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Helio 增强 Web 配置属性类
 *
 * @author Uncarbon
 */
@ConfigurationProperties(prefix = ConfigurationPropertiesPrefix.WEB)
@Data
public class HelioWebProperties {

}
