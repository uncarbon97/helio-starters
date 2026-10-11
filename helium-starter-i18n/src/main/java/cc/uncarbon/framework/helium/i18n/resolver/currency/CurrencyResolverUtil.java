package cc.uncarbon.framework.helium.i18n.resolver.currency;

import cc.uncarbon.framework.helium.i18n.context.currency.CurrencyInfo;
import cc.uncarbon.framework.helium.i18n.props.HeliumI18nProperties;
import cc.uncarbon.framework.helium.i18n.util.CurrencyUtil;
import lombok.experimental.UtilityClass;

/**
 * 多币种解析器内部工具方法
 *
 * @author Uncarbon
 */
@UtilityClass
final class CurrencyResolverUtil {

    /**
     * 是否为支持的币种：定义在 definitions 中即视为支持
     */
    boolean isSupported(HeliumI18nProperties props, String currencyCode) {
        var definitions =
                props.getCurrency() == null ? null : props.getCurrency().getDefinitions();
        return definitions != null && definitions.containsKey(currencyCode);
    }

    /**
     * 按 definitions 构造 {@link CurrencyInfo}
     */
    CurrencyInfo build(HeliumI18nProperties props, String currencyCode) {
        var def = props.getCurrency().getDefinitions().get(currencyCode);
        int scale = def.getScale() != null ? def.getScale() : CurrencyUtil.defaultScale(currencyCode);
        return CurrencyInfo.ofSimple(currencyCode, scale, def.getSymbol());
    }
}
