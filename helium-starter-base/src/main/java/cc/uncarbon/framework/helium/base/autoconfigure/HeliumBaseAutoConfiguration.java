package cc.uncarbon.framework.helium.base.autoconfigure;

import cc.uncarbon.framework.helium.base.props.HeliumBaseProperties;
import cn.hutool.http.HttpGlobalConfig;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Helium 基础自动装配类
 *
 * @author Uncarbon
 */
@EnableConfigurationProperties(value = {HeliumBaseProperties.class})
@AutoConfiguration
public class HeliumBaseAutoConfiguration {

    /**
     * hutool HttpUtil 默认超时，单位毫秒
     * 同时应用于 connectionTimeout 和 readTimeout
     */
    private static final int HUTOOL_HTTP_UTIL_DEFAULT_TIMEOUT = 30000;


    @PostConstruct
    public void postConstruct() {
        HttpGlobalConfig.setTimeout(HUTOOL_HTTP_UTIL_DEFAULT_TIMEOUT);
    }
}
