package cc.uncarbon.framework.helium.i18n.resolver.currency;

import cc.uncarbon.framework.helium.i18n.context.currency.CurrencyInfo;
import cc.uncarbon.framework.helium.i18n.util.CurrencyUtil;
import cc.uncarbon.framework.helium.i18n.props.HeliumI18nProperties;

import java.util.Map;

/**
 * 多币种解析器内部支持方法
 *
 * @author Uncarbon
 */
final class CurrencyResolverSupport {

    private CurrencyResolverSupport() {
    }

    /**
     * 是否为支持的币种：定义在 definitions 中即视为支持
     */
    static boolean isSupported(HeliumI18nProperties props, String currencyCode) {
        Map<String, HeliumI18nProperties.CurrencyDef> definitions =
                props.getCurrency() == null ? null : props.getCurrency().getDefinitions();
        return definitions != null && definitions.containsKey(currencyCode);
    }

    /**
     * 按 definitions 构造 {@link CurrencyInfo}（含 scale、symbol）
     */
    static CurrencyInfo build(HeliumI18nProperties props, String currencyCode) {
        HeliumI18nProperties.CurrencyDef def = props.getCurrency().getDefinitions().get(currencyCode);
        int scale = def.getScale() != null ? def.getScale() : CurrencyUtil.defaultScale(currencyCode);
        return CurrencyInfo.ofSimple(currencyCode, scale, def.getSymbol());
    }
}
