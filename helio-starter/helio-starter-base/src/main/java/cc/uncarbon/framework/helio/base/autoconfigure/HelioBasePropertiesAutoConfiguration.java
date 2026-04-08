package cc.uncarbon.framework.helio.base.autoconfigure;

import cc.uncarbon.framework.helio.base.props.HelioBaseProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Helio 配置属性解析自动配置类
 *
 * @author Uncarbon
 */
@EnableConfigurationProperties(value = {HelioBaseProperties.class})
@AutoConfiguration
public class HelioBasePropertiesAutoConfiguration {

}
