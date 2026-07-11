package cc.uncarbon.framework.helium.i18n.context.currency;

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
     * 取得币种代码，如 CNY、USD、USDT、BTC
     */
    String getCurrencyCode();

    /**
     * 取得精度（小数位数）
     * 法币 ISO 4217 一般为 2（JPY 等为 0），加密货币可至 6/8 等
     */
    int getScale();

    /**
     * 取得货币符号，如 ¥、$、₮；可能为空
     */
    String getSymbol();

    /**
     * 取得展示名称，如 人民币、美元、泰达币；可能为空
     */
    String getDisplayName();

    /**
     * 快速构造一个简单的 {@link CurrencyInfo} 实例
     */
    static CurrencyInfo ofSimple(final String currencyCode, final int scale, final String symbol) {
        return new SimpleCurrencyInfo(currencyCode, scale, symbol, null);
    }

    /**
     * 按 ISO 4217 币种代码构造，精度与符号取自 {@link java.util.Currency}
     *
     * @throws IllegalArgumentException 非 ISO 4217 法币代码时抛出
     */
    static CurrencyInfo ofIso(final String currencyCode) {
        java.util.Currency iso = java.util.Currency.getInstance(currencyCode);
        return new SimpleCurrencyInfo(currencyCode, iso.getDefaultFractionDigits(), iso.getSymbol(), iso.getDisplayName());
    }
}
