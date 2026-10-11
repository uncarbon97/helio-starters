package cc.uncarbon.framework.helium.i18n.message;

import cc.uncarbon.framework.helium.i18n.constant.HeliumI18nConstant;
import cc.uncarbon.framework.helium.i18n.props.HeliumI18nProperties;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 支持 YAML 格式的 MessageSource，支持嵌套 key
 * 主要适配本脚手架风格的占位符（{} 而非 {0}），没有对解析后的 {@link MessageFormat} 实例对象做缓存
 *
 * @author Uncarbon
 */
@Slf4j
public class YamlMessageSource extends AbstractMessageSource {

    protected static final String LOG_PREFIX = HeliumI18nConstant.LOG_PREFIX;
    protected static final List<String> YAML_EXTS = List.of(".yml", ".yaml");
    /**
     * messagesCache 按 locale 的容量防线（防御性措施）
     */
    protected static final int MAX_LOCALE_CACHE_SIZE = 256;
    protected final List<String> basenames;
    protected final Charset charset;
    protected final Locale fallbackLocale;
    protected final ResourcePatternResolver resourceLoader = new PathMatchingResourcePatternResolver();
    /**
     * 配置显式声明的支持语言；空列表表示未显式配置，仅预加载兜底语言与根文案
     */
    protected final List<Locale> supportedLocales;
    /**
     * 每个 locale 对应的 key -> 文案
     */
    protected final Map<Locale, Map<String, String>> messagesCache = new ConcurrentHashMap<>();


    public YamlMessageSource(@NonNull HeliumI18nProperties props, Charset charset) {
        this.basenames = props.getLang().getYamlBasenames();
        this.charset = charset;
        final String fallbackTag = props.getLang().getFallbackLanguageTag();
        this.fallbackLocale = fallbackTag != null ? Locale.forLanguageTag(fallbackTag) : null;
        this.supportedLocales = props.getLang().getSupportedLanguageTags().stream()
                .filter(CharSequenceUtil::isNotBlank).map(String::trim).map(Locale::forLanguageTag)
                .filter(locale -> !locale.getLanguage().isEmpty()).toList();
        // 直接加载
        reload();
    }

    /**
     * 按配置的支持语言，重新加载多语言文本
     */
    public void reload() {
        // 清空缓存
        messagesCache.clear();

        Set<Locale> locales = new LinkedHashSet<>();
        if (fallbackLocale != null) {
            locales.add(fallbackLocale);
        }
        for (Locale locale : supportedLocales) {
            locales.add(locale);
            if (!locale.getCountry().isEmpty()) {
                locales.add(Locale.of(locale.getLanguage()));
            }
        }
        int totalKeys = 0;
        for (Locale locale : locales) {
            totalKeys += getMessagesForLocale(locale).size();
        }
        log.info(LOG_PREFIX + "已加载 {} 个 locale，{} 条文案", locales.size(), totalKeys);
    }

    @Override
    protected MessageFormat resolveCode(@NonNull String code, @NonNull Locale locale) {
        String pattern = resolveCodeWithoutArguments(code, locale);
        return pattern != null ? new MessageFormat(pattern, locale) : null;
    }

    @Override
    protected String resolveCodeWithoutArguments(@NonNull String code, @NonNull Locale locale) {
        // 精确匹配（loadForLocale 加载时已按 默认 → 语言 → 具体区域 顺序合并覆盖）
        String value = getMessagesForLocale(locale).get(code);
        if (value != null) {
            return value;
        }

        // 兜底：仅语言部分的 locale（如 zh-CN 回退到 zh）
        if (!locale.getCountry().isEmpty()) {
            value = getMessagesForLocale(Locale.of(locale.getLanguage())).get(code);
            if (value != null) {
                return value;
            }
        }

        // 兜底：配置文件指定的 fallback 语言
        if (fallbackLocale != null && !fallbackLocale.equals(locale)) {
            value = getMessagesForLocale(fallbackLocale).get(code);
            if (value != null) {
                return value;
            }
        }

        // 兜底：根 locale（默认文案）
        if (!locale.getLanguage().isEmpty()) {
            value = getMessagesForLocale(Locale.ROOT).get(code);
            return value;
        }
        return null;
    }

    /*
    ----------------------------------------------------------------
                        私有方法
    ----------------------------------------------------------------
     */

    protected Map<String, String> loadForLocale(Locale locale) {
        Map<String, String> result = new HashMap<>();
        Yaml yaml = new Yaml();
        List<String> suffixes = localeSuffixes(locale);

        for (String basename : basenames) {
            // 从泛到具体加载，让具体区域的文案覆盖泛化的
            for (String suffix : suffixes) {
                for (String ext : YAML_EXTS) {
                    // 目录形式：basename/langTag.yml
                    if (suffix.isEmpty()) {
                        loadIfPresent(basename + ext, yaml, result);
                    } else {
                        String tag = suffix.substring(1);
                        loadIfPresent(basename + StrPool.SLASH + tag + ext, yaml, result);
                    }
                }
            }
        }
        return Collections.unmodifiableMap(result);
    }

    protected void loadIfPresent(String path, Yaml yaml, Map<String, String> result) {
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
            log.warn(LOG_PREFIX + "读取 YAML 语言包资源失败: {}", path, e);
        }
    }

    protected void loadOne(Resource resource, Yaml yaml, Map<String, String> result) {
        try {
            try (var is = resource.getInputStream()) {
                // 一个资源文件可能包含多个 YAML 文档（以 --- 分隔），逐个加载合并
                yaml.loadAll(new InputStreamReader(is, charset)).forEach(doc -> {
                    if (doc instanceof Map<?, ?> map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> raw = (Map<String, Object>) map;
                        flatten(StrUtil.EMPTY, raw, result);
                    }
                });
            }
        } catch (IOException e) {
            log.warn(LOG_PREFIX + "读取 YAML 语言包资源失败: {}", resource, e);
        } catch (YAMLException e) {
            // 捕获后按空文件处理（该语言包无翻译，走 fallback），并指明坏文件，避免每次消息查找重复抛异常
            log.warn(LOG_PREFIX + "YAML 语法错误，已跳过该语言包 >> resource={}", resource, e);
        }
    }

    /**
     * 生成从泛到具体的 locale 后缀，如 zh-CN: ["", "_zh", "_zh-CN"]
     */
    protected List<String> localeSuffixes(Locale locale) {
        List<String> suffixes = new ArrayList<>();
        suffixes.add(StrUtil.EMPTY);
        if (!locale.getLanguage().isEmpty()) {
            suffixes.add(StrPool.UNDERLINE + locale.getLanguage());
        }
        if (!locale.getCountry().isEmpty()) {
            suffixes.add(StrPool.UNDERLINE + locale.toLanguageTag());
        }
        return suffixes;
    }

    @SuppressWarnings("unchecked")
    protected void flatten(String prefix, Map<String, Object> map, Map<String, String> result) {
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

    protected Map<String, String> getMessagesForLocale(Locale locale) {
        if (messagesCache.size() >= MAX_LOCALE_CACHE_SIZE) {
            // 防御性容量防线：locale 异常多时重置缓存，避免无上界增长
            log.warn(LOG_PREFIX + "locale 缓存达到容量上限 " + MAX_LOCALE_CACHE_SIZE + "，已重置");
            messagesCache.clear();
        }
        return messagesCache.computeIfAbsent(locale, this::loadForLocale);
    }
}
