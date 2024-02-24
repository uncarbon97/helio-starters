package cc.uncarbon.framework.core.config;

import cn.hutool.http.HttpGlobalConfig;
import org.springframework.boot.autoconfigure.AutoConfiguration;

/**
 * 静态工具方法自动配置类
 */
@AutoConfiguration
public class StaticMethodAutoConfiguration {

    /**
     * hutool HttpUtil 默认超时，单位毫秒
     * 同时应用于 connectionTimeout 和 readTimeout
     */
    private static final int HUTOOL_HTTP_UTIL_DEFAULT_TIMEOUT = 30000;


    public StaticMethodAutoConfiguration() {
        HttpGlobalConfig.setTimeout(HUTOOL_HTTP_UTIL_DEFAULT_TIMEOUT);
    }
}
