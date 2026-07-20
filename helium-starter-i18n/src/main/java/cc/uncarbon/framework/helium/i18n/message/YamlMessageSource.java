package cc.uncarbon.framework.helium.i18n.message;

import cc.uncarbon.framework.helium.i18n.constant.HeliumI18nConstant;
import cc.uncarbon.framework.helium.i18n.props.HeliumI18nProperties;
import org.jspecify.annotations.NonNull;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 支持 YAML 格式的 MessageSource，支持嵌套 key
 *
 * @author Uncarbon
 */
public class YamlMessageSource extends AbstractMessageSource {

    private static final List<String> YAML_EXTS = List.of(".yml", ".yaml");

    private final List<String> basenames;
    private final Charset charset;
    private final Locale fallbackLocale;
    private final ResourceLoader resourceLoader = new PathMatchingResourcePatternResolver();

    /**
     * locale -> (key -> value)
     */
    private final Map<Locale, Map<String, String>> messagesCache = new ConcurrentHashMap<>();
    /**
     * locale -> (key -> compiled MessageFormat)
     */
    private final Map<Locale, Map<String, MessageFormat>> formatCache = new ConcurrentHashMap<>();

    public YamlMessageSource(HeliumI18nProperties props, Charset charset) {
        this.basenames = Optional.ofNullable(props)
                .map(HeliumI18nProperties::getLang)
                .map(HeliumI18nProperties.Lang::getYamlBasenames)
                .orElse(Collections.emptyList());
        this.charset = charset;
        String fallbackTag = Optional.ofNullable(props)
                .map(HeliumI18nProperties::getLang)
                .map(HeliumI18nProperties.Lang::getFallbackLanguageTag)
                .filter(tag -> !tag.isBlank())
                .orElse(null);
        this.fallbackLocale = fallbackTag != null ? Locale.forLanguageTag(fallbackTag) : null;
    }

    @Override
    protected MessageFormat resolveCode(@NonNull String code, @NonNull Locale locale) {
        Map<String, MessageFormat> formats = formatCache.computeIfAbsent(locale, l -> new ConcurrentHashMap<>());
        return formats.computeIfAbsent(code, k -> {
            String pattern = resolveCodeWithoutArguments(k, locale);
            return pattern != null ? new MessageFormat(pattern, locale) : null;
        });
    }

    @Override
    protected String resolveCodeWithoutArguments(@NonNull String code, @NonNull Locale locale) {
        // exact locale (loadForLocale already merges default → language → specific)
        String value = getMessagesForLocale(locale).get(code);
        if (value != null) {
            return value;
        }

        // fallback: language-only locale
        if (!locale.getCountry().isEmpty()) {
            value = getMessagesForLocale(Locale.of(locale.getLanguage())).get(code);
            if (value != null) {
                return value;
            }
        }

        // fallback: configured fallback language tag
        if (fallbackLocale != null && !fallbackLocale.equals(locale)) {
            value = getMessagesForLocale(fallbackLocale).get(code);
            if (value != null) {
                return value;
            }
        }

        // fallback: root (default messages)
        if (!locale.getLanguage().isEmpty()) {
            value = getMessagesForLocale(Locale.ROOT).get(code);
            return value;
        }
        return null;
    }

    private Map<String, String> getMessagesForLocale(Locale locale) {
        return messagesCache.computeIfAbsent(locale, this::loadForLocale);
    }

    private Map<String, String> loadForLocale(Locale locale) {
        Map<String, String> result = new HashMap<>();
        Yaml yaml = new Yaml();
        List<String> suffixes = localeSuffixes(locale);

        for (String basename : basenames) {
            // from generic to specific, so specific overwrites generic
            for (String suffix : suffixes) {
                for (String ext : YAML_EXTS) {
                    String path = basename + suffix + ext;
                    try {
                        Resource resource = resourceLoader.getResource(path);
                        if (!resource.exists()) continue;

                        try (var is = resource.getInputStream()) {
                            Map<String, Object> raw = yaml.load(new InputStreamReader(is, charset));
                            if (raw != null) {
                                flatten("", raw, result);
                            }
                        }
                    } catch (IOException e) {
                        logger.warn(HeliumI18nConstant.LOG_PREFIX + "Failed to load YAML message source: " + path, e);
                    }
                }
            }
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * Generate locale suffixes from generic to specific, e.g. for zh-CN: ["", "_zh", "_zh-CN"]
     */
    private List<String> localeSuffixes(Locale locale) {
        List<String> suffixes = new ArrayList<>();
        suffixes.add("");
        if (!locale.getLanguage().isEmpty()) {
            suffixes.add("_" + locale.getLanguage());
        }
        if (!locale.getCountry().isEmpty()) {
            suffixes.add("_" + locale.toLanguageTag());
        }
        return suffixes;
    }

    @SuppressWarnings("unchecked")
    private void flatten(String prefix, Map<String, Object> map, Map<String, String> result) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map) {
                flatten(key, (Map<String, Object>) value, result);
            } else if (value != null) {
                result.put(key, value.toString());
            }
        }
    }

    /**
     * Clear caches for hot reload
     */
    public void clearCache() {
        messagesCache.clear();
        formatCache.clear();
    }
}
