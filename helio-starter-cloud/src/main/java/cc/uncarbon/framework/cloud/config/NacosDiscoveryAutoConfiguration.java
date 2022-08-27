package cc.uncarbon.framework.cloud.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 启用服务发现 自动配置类
 *
 * @author Uncarbon
 */
@EnableDiscoveryClient
@AutoConfiguration
public class NacosDiscoveryAutoConfiguration {
}
