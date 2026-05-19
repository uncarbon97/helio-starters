package cc.uncarbon.framework.helio.i18n.resolver.lang;

import cc.uncarbon.framework.helio.i18n.context.LangInfo;
import cc.uncarbon.framework.helio.i18n.props.HelioI18nProperties;
import cc.uncarbon.framework.helio.i18n.resolver.LangResolver;
import cc.uncarbon.framework.helio.i18n.util.LocaleUtil;
import cn.hutool.core.text.CharSequenceUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Locale;
import java.util.Locale.LanguageRange;
import java.util.Optional;

/**
 * 从 HTTP 请求头中解析多语言
 * <ol>
 *   <li>先取自定义请求头，如「X-i18n-Lang=en_US」</li>
 *   <li>再取标准 Accept-Language 请求头，按 q 值排序匹配 supportedLocales</li>
 * </ol>
 *
 * @author Uncarbon
 */
@RequiredArgsConstructor
public class HeaderLangResolver implements LangResolver {

    static final int DEFAULT_ORDER = 20;
    private static final String HEADER_ACCEPT_LANGUAGE = "Accept-Language";

    private final HelioI18nProperties props;


    @Override
    public Optional<LangInfo> resolve(@NonNull HttpServletRequest servletRequest) {
        final String headerName = props.getLang().getResolver().getHeaderName();
        String headerVal = CharSequenceUtil.cleanBlank(servletRequest.getHeader(headerName));
        if (CharSequenceUtil.isEmpty(headerVal)) {
            return Optional.empty();
        }

        // 取标准 Accept-Language，按 q 值排序匹配 supportedLocales
        String acceptLang = servletRequest.getHeader(HEADER_ACCEPT_LANGUAGE);
        if (CharSequenceUtil.isBlank(acceptLang)) {
            return Optional.empty();
        }

        List<LanguageRange> ranges;
        try {
            ranges = LanguageRange.parse(acceptLang);
        } catch (Exception e) {
            return Optional.empty();
        }

        List<Locale> supportedLocales = props.getLang().getSupportedLanguageTags().stream()
                .map(LocaleUtil::toLocale).toList();
        for (LanguageRange range : ranges) {
            Locale matched = Locale.filter(List.of(range), supportedLocales).stream().findFirst().orElse(null);
            if (matched != null) {
                return Optional.of(LangInfo.ofSimple(matched.toLanguageTag(), matched));
            }
        }
        return Optional.empty();
    }

    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }
}
