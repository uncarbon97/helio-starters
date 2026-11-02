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

    /**
     * 启动期一次性扫描得到的「实际存在语言包」标签集（小写、'-' 分隔）；
     * 运行期未知 locale 直接归并到已知 locale，不再触发 classpath 扫描，封死 Accept-Language 轮换打 IO 的 DoS 向量
     */
    protected volatile Set<String> availableTags = Set.of();


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
        availableTags = scanAvailableTags();

        Set<Locale> locales = new LinkedHashSet<>();
        locales.add(Locale.ROOT);
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
        if (supportedLocales.isEmpty()) {
            log.warn(LOG_PREFIX + "未配置 supported-language-tags，运行期仅按实际存在的语言包响应，其余 locale 回落兜底语言 >> 可用语言包: {}", availableTags);
        }
        log.info(LOG_PREFIX + "已加载 {} 个 locale，{} 条文案；可用语言包标签: {}", locales.size(), totalKeys, availableTags);
    }

    @Override
    protected MessageFormat resolveCode(@NonNull String code, @NonNull Locale locale) {
        String pattern = resolveCodeWithoutArguments(code, locale);
        return pattern != null ? new MessageFormat(pattern, locale) : null;
    }

    @Override
    protected String resolveCodeWithoutArguments(@NonNull String code, @NonNull Locale locale) {
        // 把请求 locale 归并到实际存在语言包的 locale：未知 locale 不进缓存、不触发扫描
        Locale effective = canonicalize(locale);

        // 精确匹配（loadForLocale 加载时已按 默认 → 语言 → 具体区域 顺序合并覆盖）
        String value = getMessagesForLocale(effective).get(code);
        if (value != null) {
            return value;
        }

        // 兜底：仅语言部分的 locale（如 zh-CN 回退到 zh）
        if (!effective.getCountry().isEmpty()) {
            Locale langOnly = Locale.of(effective.getLanguage());
            if (hasLanguagePack(langOnly)) {
                value = getMessagesForLocale(langOnly).get(code);
                if (value != null) {
                    return value;
                }
            }
        }

        // 兜底：配置文件指定的 fallback 语言
        if (fallbackLocale != null && !fallbackLocale.equals(effective)) {
            value = getMessagesForLocale(fallbackLocale).get(code);
            if (value != null) {
                return value;
            }
        }

        // 兜底：根 locale（默认文案）
        return getMessagesForLocale(Locale.ROOT).get(code);
    }

    /*
    ----------------------------------------------------------------
                        私有方法
    ----------------------------------------------------------------
     */

    /**
     * 请求 locale -> 有语言包的 locale：精确命中 → 仅语言命中 → fallback → ROOT
     */
    protected Locale canonicalize(Locale locale) {
        if (locale.getLanguage().isEmpty()) {
            return Locale.ROOT;
        }
        if (hasLanguagePack(locale)) {
            return locale;
        }
        Locale langOnly = Locale.of(locale.getLanguage());
        if (hasLanguagePack(langOnly)) {
            return langOnly;
        }
        return fallbackLocale != null ? fallbackLocale : Locale.ROOT;
    }

    protected boolean hasLanguagePack(Locale locale) {
        return availableTags.contains(locale.toLanguageTag().toLowerCase());
    }

    /**
     * 启动期一次性扫描 basename 目录下的语言包文件名，得到可用标签集；仅在此处做 classpath 扫描
     */
    protected Set<String> scanAvailableTags() {
        Set<String> tags = new HashSet<>();
        for (String basename : basenames) {
            for (String ext : YAML_EXTS) {
                String pattern = basename + StrPool.SLASH + "*" + ext;
                try {
                    for (Resource resource : resourceLoader.getResources(pattern)) {
                        String filename = resource.getFilename();
                        if (filename == null) {
                            continue;
                        }
                        String tag = filename.substring(0, filename.length() - ext.length());
                        Locale parsed = Locale.forLanguageTag(tag);
                        if (!parsed.getLanguage().isEmpty()) {
                            tags.add(parsed.toLanguageTag().toLowerCase());
                        }
                    }
                } catch (IOException e) {
                    log.warn(LOG_PREFIX + "扫描语言包目录失败: {}", pattern, e);
                }
            }
        }
        // 显式配置的支持语言即使没有独立文件也视为可用（可能只靠根文案 + fallback），保证口径与配置一致
        for (Locale locale : supportedLocales) {
            tags.add(locale.toLanguageTag().toLowerCase());
        }
        if (fallbackLocale != null) {
            tags.add(fallbackLocale.toLanguageTag().toLowerCase());
        }
        return Set.copyOf(tags);
    }

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
            // 经 canonicalize 归并后键空间 = 可用语言包数 + ROOT，正常不会触达；保留作为最后防线
            log.warn(LOG_PREFIX + "locale 缓存达到容量上限 " + MAX_LOCALE_CACHE_SIZE + "，已重置");
            messagesCache.clear();
        }
        return messagesCache.computeIfAbsent(locale, this::loadForLocale);
    }
}
