package cc.uncarbon.framework.helium.i18n.context.currency;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Currency;

/**
 * 多币种信息
 *
 * <p>{@code currencyCode} 为自由字符串，兼容 ISO 4217 法币（CNY/USD 等）及非 ISO 的加密货币（USDT/BTC 等）。
 * 精度统一用 {@code scale}（小数位数）。
 *
 * @author Uncarbon
 */
public interface CurrencyInfo {

    /**
     * 按 ISO 4217 币种代码构造，精度与符号取自 {@link Currency}
     *
     * @throws IllegalArgumentException 非 ISO 4217 法币代码时抛出
     */
    static CurrencyInfo ofISO4217(final String currencyCode) {
        Currency iso = Currency.getInstance(currencyCode);
        return new SimpleCurrencyInfo(currencyCode, iso.getDefaultFractionDigits(), iso.getSymbol(), iso.getDisplayName());
    }

    /**
     * 取得精度（小数位数）
     * 法币 ISO 4217 一般为 2（JPY 等为 0），加密货币可至 6/8 等
     */
    int getScale();

    /**
     * 取得币种代码，如 CNY、USD、USDT、BTC
     */
    @NonNull
    String getCurrencyCode();

    /**
     * 取得货币符号，如 ¥、$、₮；可能为空
     */
    @Nullable
    String getSymbol();

    /**
     * 快速构造一个简单的 {@link CurrencyInfo} 实例
     */
    static CurrencyInfo ofSimple(final String currencyCode, final int scale, final String symbol) {
        return new SimpleCurrencyInfo(currencyCode, scale, symbol, null);
    }

    /**
     * 取得展示名称，如 人民币、美元、泰达币；可能为空
     */
    @Nullable
    String getDisplayName();
}
