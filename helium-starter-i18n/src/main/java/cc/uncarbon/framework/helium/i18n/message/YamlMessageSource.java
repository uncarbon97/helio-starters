package cc.uncarbon.framework.helium.i18n.message;

import cc.uncarbon.framework.helium.i18n.constant.HeliumI18nConstant;
import cc.uncarbon.framework.helium.i18n.props.HeliumI18nProperties;
import org.jspecify.annotations.NonNull;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
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

    private static final String LOG_PREFIX = HeliumI18nConstant.LOG_PREFIX;
    private static final List<String> YAML_EXTS = List.of(".yml", ".yaml");

    private final List<String> basenames;
    private final Charset charset;
    private final Locale fallbackLocale;
    private final ResourcePatternResolver resourceLoader = new PathMatchingResourcePatternResolver();

    /**
     * locale -> (key -> value)
     */
    private final Map<Locale, Map<String, String>> messagesCache = new ConcurrentHashMap<>();
    /**
     * locale -> (key -> compiled MessageFormat)
     */
    private final Map<Locale, Map<String, MessageFormat>> formatCache = new ConcurrentHashMap<>();
    /**
     * 缺失 code 的负缓存哨兵（CHM 不能存 null 值，用哨兵标记"已知缺失"，避免每次查找重跑解析链）
     */
    private static final MessageFormat MISSING_SENTINEL = new MessageFormat("");
    /**
     * messagesCache 按 locale 的容量防线（防御性：公开 API 可能传入任意 locale）
     */
    private static final int MAX_LOCALE_CACHE_SIZE = 256;

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
        MessageFormat cached = formats.get(code);
        if (cached != null) {
            return cached == MISSING_SENTINEL ? null : cached;
        }
        String pattern = resolveCodeWithoutArguments(code, locale);
        MessageFormat created = pattern != null ? createMessageFormat(pattern, locale) : MISSING_SENTINEL;
        MessageFormat previous = formats.putIfAbsent(code, created);
        if (previous != null) {
            return previous == MISSING_SENTINEL ? null : previous;
        }
        return created == MISSING_SENTINEL ? null : created;
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

    /*
    ----------------------------------------------------------------
                        私有方法 private methods
    ----------------------------------------------------------------
     */

    private Map<String, String> loadForLocale(Locale locale) {
        Map<String, String> result = new HashMap<>();
        Yaml yaml = new Yaml();
        List<String> suffixes = localeSuffixes(locale);

        for (String basename : basenames) {
            // from generic to specific, so specific overwrites generic
            for (String suffix : suffixes) {
                for (String ext : YAML_EXTS) {
                    // 目录形式：basename/langTag.yml
                    if (suffix.isEmpty()) {
                        loadIfPresent(basename + ext, yaml, result);
                    } else {
                        String tag = suffix.substring(1);
                        loadIfPresent(basename + "/" + tag + ext, yaml, result);
                    }
                }
            }
        }
        return Collections.unmodifiableMap(result);
    }

    private void loadIfPresent(String path, Yaml yaml, Map<String, String> result) {
        try {
            // classpath*: 前缀属于模式语法，需用 getResources() 复数形式匹配；getResource() 会当作字面路径而找不到
            Resource[] resources = resourceLoader.getResources(path);
            for (Resource resource : resources) {
                if (!resource.exists()) {
                    continue;
                }
                loadOne(resource, yaml, result);
            }
        } catch (IOException e) {
            logger.warn(LOG_PREFIX + "Failed to load YAML message source: " + path, e);
        }
    }

    private void loadOne(Resource resource, Yaml yaml, Map<String, String> result) {
        try {
            try (var is = resource.getInputStream()) {
                // 一个资源文件可能包含多个 YAML 文档（以 --- 分隔），逐个加载合并
                yaml.loadAll(new InputStreamReader(is, charset)).forEach(doc -> {
                    if (doc instanceof Map<?, ?> map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> raw = (Map<String, Object>) map;
                        flatten("", raw, result);
                    }
                });
            }
        } catch (IOException e) {
            logger.warn(LOG_PREFIX + "Failed to load YAML message source: " + resource, e);
        } catch (RuntimeException e) {
            /*
            snakeyaml 语法错误抛 YAMLException（RuntimeException）：
            捕获后按空文件处理（该语言包无翻译，走 fallback），并指明坏文件，避免每次消息查找重复抛异常
             */
            logger.warn(LOG_PREFIX + "YAML 语法错误，已跳过该语言包 >> resource=" + resource, e);
        }
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

    /**
     * 构造 MessageFormat；文案含非法花括号（如未转义的 JSON 示例）时转义后按字面输出原文，而非抛 IllegalArgumentException
     */
    protected @NonNull MessageFormat createMessageFormat(@NonNull String pattern, Locale locale) {
        try {
            return new MessageFormat(pattern, locale);
        } catch (IllegalArgumentException e) {
            logger.warn(LOG_PREFIX + "消息文案含 MessageFormat 非法花括号，已回退为纯文本输出 >> pattern=" + pattern);
            String escaped = pattern.replace("'", "''").replace("{", "'{'").replace("}", "'}'");
            return new MessageFormat(escaped, locale);
        }
    }

    private Map<String, String> getMessagesForLocale(Locale locale) {
        if (messagesCache.size() >= MAX_LOCALE_CACHE_SIZE) {
            // 防御性容量防线：异常多的 locale（如误把用户输入当 locale 传入）时重置缓存，避免无上界增长
            logger.warn(LOG_PREFIX + "locale 缓存达到容量上限 " + MAX_LOCALE_CACHE_SIZE + "，已重置");
            messagesCache.clear();
        }
        return messagesCache.computeIfAbsent(locale, this::loadForLocale);
    }
}
