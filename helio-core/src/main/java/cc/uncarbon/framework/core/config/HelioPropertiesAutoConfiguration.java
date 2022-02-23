package cc.uncarbon.framework.core.config;

import cc.uncarbon.framework.core.props.HelioProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 自动配置文件读取
 *
 * @author Uncarbon
 */
@EnableConfigurationProperties(value = {HelioProperties.class})
@Configuration
public class HelioPropertiesAutoConfiguration {

}
