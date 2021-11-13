package cc.uncarbon.framework.core.config;

import cc.uncarbon.framework.core.props.AliyunRocketProperties;
import cc.uncarbon.framework.core.props.HelioProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Uncarbon
 */
@EnableConfigurationProperties(value = {HelioProperties.class, AliyunRocketProperties.class})
@Configuration
public class HelioPropertiesAutoConfiguration {
}
