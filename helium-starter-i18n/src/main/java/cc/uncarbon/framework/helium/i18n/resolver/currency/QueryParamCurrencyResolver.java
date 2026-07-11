package cc.uncarbon.framework.helium.i18n.resolver.currency;

import cc.uncarbon.framework.helium.i18n.context.currency.CurrencyInfo;
import cc.uncarbon.framework.helium.i18n.props.HeliumI18nProperties;
import cn.hutool.core.text.CharSequenceUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

/**
 * 从 URL 请求参数中解析多币种，如「?currency=USDT」
 *
 * @author Uncarbon
 */
@RequiredArgsConstructor
public class QueryParamCurrencyResolver implements CurrencyResolver {

    static final int DEFAULT_ORDER = 10;

    private final HeliumI18nProperties props;

    @Override
    public Optional<CurrencyInfo> resolve(@NonNull HttpServletRequest servletRequest) {
        var cfg = props.getCurrency();
        if (cfg == null || cfg.getResolver() == null) {
            return Optional.empty();
        }
        final String paramName = cfg.getResolver().getQueryParamName();
        String paramVal = CharSequenceUtil.cleanBlank(servletRequest.getParameter(paramName));
        if (CharSequenceUtil.isEmpty(paramVal) || !CurrencyResolverSupport.isSupported(props, paramVal)) {
            return Optional.empty();
        }
        return Optional.of(CurrencyResolverSupport.build(props, paramVal));
    }

    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }
}
