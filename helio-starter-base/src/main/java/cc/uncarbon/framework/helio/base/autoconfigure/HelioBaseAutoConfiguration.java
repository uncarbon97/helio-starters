package cc.uncarbon.framework.helio.base.autoconfigure;

import cc.uncarbon.framework.helio.base.props.HelioBaseProperties;
import cn.hutool.http.HttpGlobalConfig;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Helio 基础自动配置类
 *
 * @author Uncarbon
 */
@EnableConfigurationProperties(value = {HelioBaseProperties.class})
@AutoConfiguration
public class HelioBaseAutoConfiguration {

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
