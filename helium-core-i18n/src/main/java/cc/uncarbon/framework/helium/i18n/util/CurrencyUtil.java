package cc.uncarbon.framework.helium.i18n.util;

import cc.uncarbon.framework.helium.i18n.context.currency.CurrencyInfo;
import cc.uncarbon.framework.helium.i18n.model.Money;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * 多币种格式化工具
 *
 * @author Uncarbon
 */
@UtilityClass
public class CurrencyUtil {

    /**
     * 按 {@link CurrencyInfo} 的符号、给定 {@link Locale} 格式化金额
     *
     * <p>ISO 4217 法币走 {@link NumberFormat#getCurrencyInstance(Locale)}；
     * 非 ISO（加密货币等）回退为 {@code 符号 + 定点小数}。
     */
    public static String format(Money money, CurrencyInfo info, Locale locale) {
        Locale targetLocale = locale != null ? locale : Locale.getDefault();
        if (isIsoCurrency(money.currencyCode())) {
            try {
                NumberFormat nf = NumberFormat.getCurrencyInstance(targetLocale);
                nf.setCurrency(java.util.Currency.getInstance(money.currencyCode()));
                nf.setMinimumFractionDigits(money.scale());
                nf.setMaximumFractionDigits(money.scale());
                return nf.format(money.amount());
            } catch (IllegalArgumentException ignored) {
                // 走非 ISO 回退
            }
        }
        String symbol = info != null && info.getSymbol() != null && !info.getSymbol().isBlank()
                ? info.getSymbol()
                : money.currencyCode();
        return symbol + money.amount().toPlainString();
    }

    /**
     * 仅按 {@link Locale} 格式化金额（无额外元数据；非 ISO 币种以 {@code 代码 + 定点小数} 展示）
     */
    public static String format(Money money, Locale locale) {
        return format(money, null, locale);
    }

    /**
     * 无符号、定点小数字符串
     */
    public static String formatPlain(Money money) {
        return money.amount().toPlainString();
    }

    /**
     * 取规整后的数值（已按 scale 四舍五入 HALF_EVEN）
     */
    public static BigDecimal round(Money money) {
        return money.amount();
    }

    /**
     * ISO 4217 币种代码的默认精度，非 ISO 返回 2
     */
    public static int defaultScale(String currencyCode) {
        try {
            return java.util.Currency.getInstance(currencyCode).getDefaultFractionDigits();
        } catch (Exception e) {
            return 2;
        }
    }

    private static boolean isIsoCurrency(String currencyCode) {
        try {
            java.util.Currency.getInstance(currencyCode);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
