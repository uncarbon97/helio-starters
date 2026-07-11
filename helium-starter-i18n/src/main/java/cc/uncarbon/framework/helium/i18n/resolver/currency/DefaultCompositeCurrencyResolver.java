package cc.uncarbon.framework.helium.i18n.resolver.currency;

import cc.uncarbon.framework.helium.i18n.constant.HeliumI18nConstant;
import cc.uncarbon.framework.helium.i18n.context.currency.CurrencyInfo;
import cc.uncarbon.framework.helium.i18n.util.CurrencyUtil;
import cc.uncarbon.framework.helium.i18n.props.HeliumI18nProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * 组合多币种解析器，按 order 升序尝试各解析器，返回第一个有效结果，兜底返回默认币种
 *
 * @author Uncarbon
 */
@Slf4j
public class DefaultCompositeCurrencyResolver implements CompositeCurrencyResolver {

    private static final String LOG_PREFIX = HeliumI18nConstant.LOG_PREFIX + "[CompositeCurrencyResolver]";

    private final HeliumI18nProperties props;
    private final List<CurrencyResolver> resolvers;

    public DefaultCompositeCurrencyResolver(HeliumI18nProperties props, List<CurrencyResolver> resolvers) {
        this.props = props;
        this.resolvers = resolvers.stream()
                .sorted(Comparator.comparingInt(CurrencyResolver::getOrder))
                .toList();
    }

    @Override
    public CurrencyInfo resolve(HttpServletRequest servletRequest) {
        for (CurrencyResolver resolver : resolvers) {
            try {
                Optional<CurrencyInfo> curInfoOp = resolver.resolve(servletRequest);
                if (curInfoOp.isPresent()) {
                    CurrencyInfo curInfo = curInfoOp.get();
                    if (CurrencyResolverSupport.isSupported(props, curInfo.getCurrencyCode())) {
                        return curInfo;
                    }
                }
            } catch (Exception e) {
                log.warn(LOG_PREFIX + "resolve failed", e);
            }
        }
        // 兜底返回默认币种
        var cfg = props.getCurrency();
        String defaultCurrency = cfg != null ? cfg.getDefaultCurrency() : null;
        if (defaultCurrency == null || defaultCurrency.isBlank()) {
            // 未配置币种时的最终兜底
            return CurrencyInfo.ofSimple("CNY", CurrencyUtil.defaultScale("CNY"), null);
        }
        if (CurrencyResolverSupport.isSupported(props, defaultCurrency)) {
            return CurrencyResolverSupport.build(props, defaultCurrency);
        }
        // 默认币种未在 definitions 中定义：以默认精度兜底
        return CurrencyInfo.ofSimple(defaultCurrency, CurrencyUtil.defaultScale(defaultCurrency), null);
    }
}
