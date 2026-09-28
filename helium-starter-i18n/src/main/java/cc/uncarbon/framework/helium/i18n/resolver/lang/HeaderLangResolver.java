package cc.uncarbon.framework.helium.i18n.resolver.lang;

import cc.uncarbon.framework.helium.i18n.context.lang.LangInfo;
import cc.uncarbon.framework.helium.i18n.props.HeliumI18nProperties;
import cc.uncarbon.framework.helium.i18n.util.LocaleUtil;
import cn.hutool.core.text.CharSequenceUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Locale;
import java.util.Locale.LanguageRange;
import java.util.Objects;
import java.util.Optional;

/**
 * 从 HTTP 请求头中解析多语言
 * <ol>
 *   <li>先取自定义请求头，如「X-i18n-Lang=en_US」</li>
 *   <li>未命中或不支持时，再取标准 Accept-Language 请求头，按 q 值排序匹配 supportedLocales</li>
 * </ol>
 *
 * @author Uncarbon
 */
@RequiredArgsConstructor
public class HeaderLangResolver implements LangResolver {

    static final int DEFAULT_ORDER = 20;
    private static final String HEADER_ACCEPT_LANGUAGE = "Accept-Language";

    private final HeliumI18nProperties props;


    @Override
    public Optional<LangInfo> resolve(@NonNull HttpServletRequest servletRequest) {
        var langCfg = props.getLang();
        var cfg = langCfg != null ? langCfg.getResolver() : null;

        // 1. 先取自定义请求头，如「X-i18n-Lang=en_US」（配置缺失时跳过本步骤）
        if (cfg != null && cfg.getHeaderName() != null) {
            String headerVal = CharSequenceUtil.cleanBlank(servletRequest.getHeader(cfg.getHeaderName()));
            if (CharSequenceUtil.isNotEmpty(headerVal)) {
                Locale headerLocale = toLocaleSafely(headerVal);
                if (headerLocale != null) {
                    String languageTag = headerLocale.toLanguageTag();
                    if (isSupported(langCfg, languageTag)) {
                        return Optional.of(LangInfo.ofSimple(languageTag, headerLocale));
                    }
                }
            }
        }

        // 2. 取标准 Accept-Language，按 q 值排序匹配 supportedLocales
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

        // 3. 语言前缀模糊匹配：Locale.filter 可将 en 匹配到 en-US 等（LanguageRange 基础过滤）
        if (langCfg == null || langCfg.getSupportedLanguageTags() == null) {
            return Optional.empty();
        }
        List<Locale> supportedLocales = langCfg.getSupportedLanguageTags().stream()
                .map(this::toLocaleSafely)
                .filter(Objects::nonNull)
                .toList();
        for (LanguageRange range : ranges) {
            Locale matched = Locale.filter(List.of(range), supportedLocales).stream().findFirst().orElse(null);
            if (matched != null) {
                return Optional.of(LangInfo.ofSimple(matched.toLanguageTag(), matched));
            }
        }
        return Optional.empty();
    }

    /**
     * 非法语言标签（如长度<2、格式错误）按未命中处理，而非抛 IllegalArgumentException
     */
    private Locale toLocaleSafely(String tag) {
        try {
            return LocaleUtil.toLocale(tag);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private boolean isSupported(HeliumI18nProperties.Lang langCfg, String languageTag) {
        List<String> tags = langCfg.getSupportedLanguageTags();
        // 未配置支持语言集时不做校验，避免未配置用户 i18n 整体失效
        return tags == null || tags.isEmpty() || tags.contains(languageTag);
    }

    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }
}
