package cc.uncarbon.framework.helio.i18n.resolver.lang;

import cc.uncarbon.framework.helio.i18n.props.HelioI18nProperties;
import cc.uncarbon.framework.helio.i18n.resolver.LangResolver;
import cc.uncarbon.framework.helio.i18n.util.I18nParser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import java.util.Locale;
import java.util.Optional;

/**
 * 从 URL 请求参数中解析多语言，如「?lang=en_US」
 *
 * @author Uncarbon
 */
@RequiredArgsConstructor
public class QueryParamLangResolver implements LangResolver {

    static final int DEFAULT_ORDER = 10;

    private final HelioI18nProperties props;


    @Override
    public Optional<Locale> resolve(HttpServletRequest request) {
        String paramName = props.getLang().getResolver().getQueryParamName();
        String value = request.getParameter(paramName);
        return I18nParser.parseLocale(value);
    }

    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }
}
