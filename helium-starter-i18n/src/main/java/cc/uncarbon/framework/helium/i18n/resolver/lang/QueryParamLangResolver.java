package cc.uncarbon.framework.helium.i18n.resolver.lang;

import cc.uncarbon.framework.helium.i18n.context.lang.LangInfo;
import cc.uncarbon.framework.helium.i18n.props.HeliumI18nProperties;
import cc.uncarbon.framework.helium.i18n.util.LocaleUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;

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

    private final HeliumI18nProperties props;


    @Override
    public Optional<LangInfo> resolve(@NonNull HttpServletRequest servletRequest) {
        final String paramName = props.getLang().getResolver().getQueryParamName();
        String paramVal = CharSequenceUtil.cleanBlank(servletRequest.getParameter(paramName));
        if (CharSequenceUtil.isEmpty(paramVal)) {
            return Optional.empty();
        }
        Locale visitorLocale = LocaleUtil.toLocale(paramVal);
        if (visitorLocale == null) {
            return Optional.empty();
        }

        String languageTag = visitorLocale.toLanguageTag();
        if (isSupported(languageTag)) {
            return Optional.of(LangInfo.ofSimple(languageTag, visitorLocale));
        }
        return Optional.empty();
    }

    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }

    private boolean isSupported(String languageTag) {
        return CollUtil.contains(props.getLang().getSupportedLanguageTags(), languageTag);
    }
}
