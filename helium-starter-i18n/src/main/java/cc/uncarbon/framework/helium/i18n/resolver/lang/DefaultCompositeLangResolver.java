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


    public DefaultCompositeLangResolver(HeliumI18nProperties props, List<LangResolver> resolvers) {
        this.props = props;
        this.resolvers = resolvers.stream()
                .sorted(Comparator.comparingInt(LangResolver::getOrder))
                .toList();
    }

    @Override
    public LangInfo resolve(HttpServletRequest request) {
        var langCfg = props.getLang();
        for (LangResolver resolver : resolvers) {
            try {
                var langInfoOp = resolver.resolve(request);
                if (langInfoOp.isPresent()) {
                    LangInfo langInfo = langInfoOp.get();
                    if (isSupported(langCfg, langInfo.getLanguageTag())) {
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

    private boolean isSupported(HeliumI18nProperties.Lang langCfg, String languageTag) {
        List<String> tags = langCfg != null ? langCfg.getSupportedLanguageTags() : null;
        // 未配置支持语言集时不做校验
        return tags == null || tags.isEmpty() || tags.contains(languageTag);
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
