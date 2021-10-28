package cc.uncarbon.framework.core.config;

import cc.uncarbon.framework.core.props.HelioProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Uncarbon
 */
@EnableConfigurationProperties(HelioProperties.class)
@Configuration
public class EnableConfigurationPropertiesAutoConfiguration {
}
