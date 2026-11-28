package cc.uncarbon.framework.helium.i18n.resolver.lang;

import cc.uncarbon.framework.helium.i18n.constant.HeliumI18nConstant;
import cc.uncarbon.framework.helium.i18n.context.lang.LangInfo;
import cc.uncarbon.framework.helium.i18n.props.HeliumI18nProperties;
import cc.uncarbon.framework.helium.i18n.util.LocaleUtil;
import cn.hutool.core.text.CharSequenceUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 组合多语言解析器，按 order 升序尝试各解析器，返回第一个有效结果，兜底返回默认语言
 *
 * @author Uncarbon
 */
@Slf4j
public class DefaultCompositeLangResolver implements CompositeLangResolver {

    private static final String LOG_PREFIX = HeliumI18nConstant.LOG_PREFIX + "[CompositeLangResolver]";


    private final HeliumI18nProperties props;
    private final List<LangResolver> resolvers;

    /**
     * 归一化后的支持语言集（'-' 分隔、小写）；空集 = 未配置、不做校验
     */
    private final Set<String> normalizedSupportedTags;


    public DefaultCompositeLangResolver(HeliumI18nProperties props, List<LangResolver> resolvers) {
        this.props = props;
        this.resolvers = resolvers.stream()
                .sorted(Comparator.comparingInt(LangResolver::getOrder))
                .toList();
        var langCfg = props.getLang();
        List<String> tags = langCfg != null ? langCfg.getSupportedLanguageTags() : null;
        this.normalizedSupportedTags = tags == null ? Set.of() : tags.stream()
                .filter(CharSequenceUtil::isNotBlank)
                .map(LocaleUtil::normalizeLanguageTag)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public LangInfo resolve(HttpServletRequest request) {
        var langCfg = props.getLang();
        for (LangResolver resolver : resolvers) {
            try {
                var langInfoOp = resolver.resolve(request);
                if (langInfoOp.isPresent()) {
                    LangInfo langInfo = langInfoOp.get();
                    if (isSupported(langInfo.getLanguageTag())) {
                        return langInfo;
                    }
                }
            } catch (Exception e) {
                log.warn(LOG_PREFIX + "resolve failed", e);
            }
        }
        // 兜底返回默认语言；默认语言缺配时回落兜底语言，均为空时回落 JVM 默认 Locale，避免产出全 null 的 LangInfo
        String defaultTag = langCfg != null && CharSequenceUtil.isNotBlank(langCfg.getDefaultLanguageTag())
                ? langCfg.getDefaultLanguageTag()
                : (langCfg != null ? langCfg.getFallbackLanguageTag() : null);
        Locale defaultLocale = toLocaleSafely(defaultTag);
        if (defaultLocale == null) {
            defaultLocale = Locale.getDefault();
        }
        return LangInfo.ofSimple(defaultLocale.toLanguageTag(), defaultLocale);
    }

    /**
     * 归一化后比较，使配置 {@code en_US} / 请求 {@code en-US} / {@code EN-us} 等价；
     * 精确标签未命中时再按语言主标签（{@code en-US} -> {@code en}）回退匹配
     */
    private boolean isSupported(String languageTag) {
        // 未配置支持语言集时不做校验
        if (normalizedSupportedTags.isEmpty()) {
            return true;
        }
        String normalized = LocaleUtil.normalizeLanguageTag(languageTag);
        if (normalizedSupportedTags.contains(normalized)) {
            return true;
        }
        int dash = normalized.indexOf('-');
        return dash > 0 && normalizedSupportedTags.contains(normalized.substring(0, dash));
    }

    private Locale toLocaleSafely(String tag) {
        if (CharSequenceUtil.isBlank(tag)) {
            return null;
        }
        try {
            return LocaleUtil.toLocale(tag);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
