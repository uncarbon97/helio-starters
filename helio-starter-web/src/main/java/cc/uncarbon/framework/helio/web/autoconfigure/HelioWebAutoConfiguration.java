package cc.uncarbon.framework.helio.web.autoconfigure;

import cc.uncarbon.framework.helio.web.configurer.EnhanceWebMvcConfigurer;
import cc.uncarbon.framework.helio.web.handler.GlobalWebExceptionHandler;
import cc.uncarbon.framework.helio.web.listener.WebServerLaunchedListener;
import cc.uncarbon.framework.helio.web.props.HelioWebProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;

/**
 * Helio 增强 Web 自动配置类
 *
 * @author Uncarbon
 */
@Import(value = {EnhanceWebMvcConfigurer.class, GlobalWebExceptionHandler.class, WebServerLaunchedListener.class})
@EnableConfigurationProperties(value = HelioWebProperties.class)
@AutoConfiguration
public class HelioWebAutoConfiguration {
}
