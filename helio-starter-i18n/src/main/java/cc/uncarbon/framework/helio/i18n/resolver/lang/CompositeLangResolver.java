package cc.uncarbon.framework.helio.i18n.resolver.lang;

import cc.uncarbon.framework.helio.i18n.props.HelioI18nProperties;
import cc.uncarbon.framework.helio.i18n.resolver.LangResolver;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * 组合多语言解析器，按 order 升序尝试各解析器，返回第一个有效结果
 *
 * @author Uncarbon
 */
@Slf4j
public class CompositeLangResolver {

    private final HelioI18nProperties props;
    private final List<LangResolver> resolvers;


    public CompositeLangResolver(HelioI18nProperties props, List<LangResolver> resolvers) {
        this.props = props;
        this.resolvers = resolvers.stream()
                .sorted(Comparator.comparingInt(LangResolver::getOrder))
                .toList();
    }

    public Locale resolve(HttpServletRequest request) {
        for (LangResolver resolver : resolvers) {
            try {
                var locale = resolver.resolve(request);
                if (locale.isPresent() && isSupported(locale.get())) {
                    return locale.get();
                }
            } catch (Exception e) {
                log.warn("Lang resolver {} failed", resolver.getClass().getSimpleName(), e);
            }
        }
        return props.getLang().getDefaultLanguageTag();
    }

    private boolean isSupported(Locale locale) {
        return props.getLang().getSupportedLanguageTags().contains(locale);
    }
}
