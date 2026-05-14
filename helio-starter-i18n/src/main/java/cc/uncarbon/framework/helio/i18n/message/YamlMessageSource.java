package cc.uncarbon.framework.helio.i18n.message;

import io.micrometer.common.util.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 支持 YAML 格式的 MessageSource，支持嵌套 key
 *
 * messages.yml:
 *   user:
 *     error:
 *       notFound: "用户不存在"
 *       email:
 *         alreadyExists: "邮箱 {0} 已被注册"
 *
 * 等价于：
 *   user.error.notFound=用户不存在
 *   user.error.email.alreadyExists=邮箱 {0} 已被注册
 */
public class YamlMessageSource extends AbstractMessageSource {

    private final List<String> basenames;
    private final String encoding;
    private final ResourceLoader resourceLoader = new PathMatchingResourcePatternResolver();

    /** locale -> (key -> value) */
    private final Map<Locale, Map<String, String>> cache = new ConcurrentHashMap<>();
    /** locale -> (key -> compiled MessageFormat) */
    private final Map<Locale, Map<String, MessageFormat>> formatCache = new ConcurrentHashMap<>();

    public YamlMessageSource(List<String> basenames, String encoding) {
        this.basenames = basenames;
        this.encoding = encoding;
    }

    @Override
    protected MessageFormat resolveCode(@NonNull String code, @NonNull Locale locale) {
        Map<String, MessageFormat> formats = formatCache.computeIfAbsent(locale, l -> new ConcurrentHashMap<>());
        return formats.computeIfAbsent(code, k -> {
            String pattern = resolveCodeWithoutArguments(k, locale);
            return pattern == null ? null : new MessageFormat(pattern, locale);
        });
    }

    @Override
    protected String resolveCodeWithoutArguments(@NonNull String code, @NonNull Locale locale) {
        Map<String, String> messages = getMessagesForLocale(locale);
        String value = messages.get(code);
        if (value != null) return value;

        // 降级：zh-CN -> zh -> default
        for (Locale fallback : LocaleUtils.fallbackChain(locale)) {
            value = getMessagesForLocale(fallback).get(code);
            if (value != null) return value;
        }
        return null;
    }

    private Map<String, String> getMessagesForLocale(Locale locale) {
        return cache.computeIfAbsent(locale, this::loadForLocale);
    }

    private Map<String, String> loadForLocale(Locale locale) {
        Map<String, String> result = new HashMap<>();
        Yaml yaml = new Yaml();

        for (String basename : basenames) {
            // 尝试加载 messages_zh_CN.yml, messages_zh.yml, messages.yml
            for (String suffix : localeSuffixes(locale)) {
                for (String ext : List.of(".yml", ".yaml")) {
                    String path = basename + suffix + ext;
                    try {
                        Resource resource = resourceLoader.getResource(path);
                        if (!resource.exists()) continue;

                        try (var is = resource.getInputStream()) {
                            Map<String, Object> raw = yaml.load(new InputStreamReader(is, encoding));
                            if (raw != null) flatten("", raw, result);
                        }
                    } catch (IOException e) {
                        logger.warn("Failed to load yaml message source: " + path, e);
                    }
                }
            }
        }
        return result;
    }

    /** [_zh_CN, _zh, ""] */
    private List<String> localeSuffixes(Locale locale) {
        List<String> suffixes = new ArrayList<>();
        if (StringUtils.isNotBlank(locale.getCountry())) {
            suffixes.add("_" + locale.getLanguage() + "_" + locale.getCountry());
        }
        if (StringUtils.isNotBlank(locale.getLanguage())) {
            suffixes.add("_" + locale.getLanguage());
        }
        suffixes.add(""); // 默认 messages.yml
        return suffixes;
    }

    /** 递归展平嵌套 map */
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

    /** 清除缓存（用于热更新） */
    public void clearCache() {
        cache.clear();
        formatCache.clear();
    }
}
