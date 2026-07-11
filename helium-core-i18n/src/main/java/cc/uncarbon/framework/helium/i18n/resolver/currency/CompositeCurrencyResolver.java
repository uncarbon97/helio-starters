package cc.uncarbon.framework.helium.i18n.resolver.currency;

import cc.uncarbon.framework.helium.i18n.context.currency.CurrencyInfo;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 组合后的多币种解析器
 */
public interface CompositeCurrencyResolver {

    /**
     * 解析为 {@link CurrencyInfo} 实例
     */
    CurrencyInfo resolve(HttpServletRequest servletRequest);

}
