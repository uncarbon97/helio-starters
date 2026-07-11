package cc.uncarbon.framework.helium.i18n.resolver.currency;

import cc.uncarbon.framework.helium.i18n.context.currency.CurrencyInfo;
import cc.uncarbon.framework.helium.i18n.props.HeliumI18nProperties;
import cn.hutool.core.text.CharSequenceUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

/**
 * 从 HTTP 请求头中解析多币种，如「X-i18n-Currency=USDT」
 *
 * @author Uncarbon
 */
@RequiredArgsConstructor
public class HeaderCurrencyResolver implements CurrencyResolver {

    static final int DEFAULT_ORDER = 20;

    private final HeliumI18nProperties props;

    @Override
    public Optional<CurrencyInfo> resolve(@NonNull HttpServletRequest servletRequest) {
        var cfg = props.getCurrency();
        if (cfg == null || cfg.getResolver() == null) {
            return Optional.empty();
        }
        final String headerName = cfg.getResolver().getHeaderName();
        String headerVal = CharSequenceUtil.cleanBlank(servletRequest.getHeader(headerName));
        if (CharSequenceUtil.isEmpty(headerVal) || !CurrencyResolverSupport.isSupported(props, headerVal)) {
            return Optional.empty();
        }
        return Optional.of(CurrencyResolverSupport.build(props, headerVal));
    }

    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }
}
