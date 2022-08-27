package cc.uncarbon.framework.cloud.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * 启用配置自动刷新 自动配置类
 *
 * @author Uncarbon
 */
@RefreshScope
@AutoConfiguration
public class NacosConfigAutoConfiguration {
}
