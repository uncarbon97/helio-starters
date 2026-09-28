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
        var langCfg = props.getLang();
        var cfg = langCfg != null ? langCfg.getResolver() : null;
        // 配置缺失时跳过本解析器（交给后续解析器/兜底），而非 NPE
        if (cfg == null || cfg.getQueryParamName() == null) {
            return Optional.empty();
        }

        String paramVal = CharSequenceUtil.cleanBlank(servletRequest.getParameter(cfg.getQueryParamName()));
        if (CharSequenceUtil.isEmpty(paramVal)) {
            return Optional.empty();
        }
        Locale visitorLocale = toLocaleSafely(paramVal);
        if (visitorLocale == null) {
            return Optional.empty();
        }

        String languageTag = visitorLocale.toLanguageTag();
        if (isSupported(langCfg, languageTag)) {
            return Optional.of(LangInfo.ofSimple(languageTag, visitorLocale));
        }
        return Optional.empty();
    }

    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
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
}
