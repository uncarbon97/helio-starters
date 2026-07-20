package cc.uncarbon.framework.helium.i18n.props;

import cc.uncarbon.framework.helium.base.constant.ConfigurationPropertiesPrefix;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

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
    private Lang lang;

    /**
     * 多时区
     */
    private Timezone timezone;

    /**
     * 多币种
     */
    private Currency currency;


    @Data
    public static class Lang {

        /**
         * 指定默认语言
         */
        private String defaultLanguageTag;

        /**
         * 兜底语言标签
         * 当请求语言、语言前缀都未命中翻译时，回退到此语言；留空则回退到 ROOT
         */
        private String fallbackLanguageTag;

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
    public static class Timezone {

        /**
         * 数据时区
         */
        private String dbTimezone;

        /**
         * 显式标注支持的显示时区，留空则不校验、接受任意合法 ZoneId
         */
        private List<String> supportedDisplayTimezones;

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

    @Data
    public static class Currency {

        /**
         * 指定默认币种
         */
        private String defaultCurrency;

        /**
         * 币种定义，key 为币种码；keys() 同时即“支持的币种集”
         * <p>显式定义即视为支持，无需再单独维护 supported-currencies
         */
        private Map<String, CurrencyDef> definitions;

        /**
         * 多币种解析器子配置属性
         */
        private CurrencyResolverConfig resolver;

    }

    @Data
    public static class CurrencyDef {

        /**
         * 精度（小数位数）；留空则按 ISO 4217 默认值，非 ISO 默认 2
         */
        private Integer scale;

        /**
         * 货币符号
         */
        private String symbol;

        /**
         * 展示名称
         */
        private String displayName;

    }

    @Data
    public static class CurrencyResolverConfig {

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
