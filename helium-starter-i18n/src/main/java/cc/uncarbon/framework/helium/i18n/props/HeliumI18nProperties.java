package cc.uncarbon.framework.helium.i18n.props;

import cc.uncarbon.framework.helium.base.constant.ConfigurationPropertiesPrefix;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Helium 集成国际化配置属性类
 *
 * @author Uncarbon
 */
@ConfigurationProperties(prefix = ConfigurationPropertiesPrefix.I18N)
@Data
public class HeliumI18nProperties {

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 多语言
     */
    private LangConfig lang;

    /**
     * 多时区
     */
    private TimezoneConfig timezone;


    @Data
    public static class LangConfig {

        /**
         * 指定默认语言
         */
        private String defaultLanguageTag;

        /**
         * 显式标注支持的语言
         */
        private List<String> supportedLanguageTags;

        /**
         * 本地以 YAML 形式维护的语言包，主文件夹列表
         */
        private List<String> yamlBasenames;

        /**
         * 多语言解析器子配置属性
         */
        private LangResolverConfig resolver;

    }

    @Data
    public static class TimezoneConfig {

        /**
         * 指定默认时区
         */
        private String defaultTimezone;

        /**
         * 多时区解析器子配置属性
         */
        private TimezoneResolverConfig resolver;

    }

    @Data
    public static class LangResolverConfig {

        /**
         * URL 请求参数名
         */
        private String queryParamName;

        /**
         * HTTP 请求头名
         */
        private String headerName;

    }

    @Data
    public static class TimezoneResolverConfig {

        /**
         * URL 请求参数名
         */
        private String queryParamName;

        /**
         * HTTP 请求头名
         */
        private String headerName;

    }
}
