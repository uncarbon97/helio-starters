package cc.uncarbon.framework.core.config;

import cc.uncarbon.framework.core.props.HelioProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * properties 解析自动配置类
 *
 * @author Uncarbon
 */
@EnableConfigurationProperties(value = {HelioProperties.class})
@AutoConfiguration
public class HelioPropertiesAutoConfiguration {

}
