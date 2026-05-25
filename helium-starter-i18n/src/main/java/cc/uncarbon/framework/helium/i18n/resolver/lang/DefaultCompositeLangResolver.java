package cc.uncarbon.framework.helium.i18n.resolver.lang;

import cc.uncarbon.framework.helium.i18n.constant.HeliumI18nConstant;
import cc.uncarbon.framework.helium.i18n.context.LangInfo;
import cc.uncarbon.framework.helium.i18n.props.HeliumI18nProperties;
import cc.uncarbon.framework.helium.i18n.resolver.CompositeLangResolver;
import cc.uncarbon.framework.helium.i18n.resolver.LangResolver;
import cc.uncarbon.framework.helium.i18n.util.LocaleUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;

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
        // 兜底返回默认语言
        return LangInfo.ofSimple(
                props.getLang().getDefaultLanguageTag(),
                LocaleUtil.toLocale(props.getLang().getDefaultLanguageTag()));
    }

    private boolean isSupported(String languageTag) {
        return props.getLang().getSupportedLanguageTags().contains(languageTag);
    }
}
