package cc.uncarbon.framework.helium.i18n.model;

import cc.uncarbon.framework.helium.i18n.context.currency.CurrencyInfo;
import org.jspecify.annotations.NonNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * 不可变金额值类型（原存原取，不做汇率转换）
 *
 * <p>{@code amount} 在构造时按 {@code scale} 规整（{@link RoundingMode#HALF_EVEN}）；
 * 全程 {@link BigDecimal}，不丢精度，支持加密货币高精度（如 USDT scale=6、BTC scale=8）。
 * 运算要求双方 {@code currencyCode} 一致，否则抛出 {@link IllegalArgumentException}；
 * {@link #compareTo} 为全序比较（先币种后金额）以满足 {@link Comparable} 契约，同币种金额比较请用 {@link #compareAmount}。
 *
 * @author Uncarbon
 */
public record Money(BigDecimal amount, String currencyCode, int scale) implements Comparable<Money> {

    public Money {
        Objects.requireNonNull(amount, "amount 不能为空");
        Objects.requireNonNull(currencyCode, "currencyCode 不能为空");
        if (currencyCode.isBlank()) {
            throw new IllegalArgumentException("currencyCode 不能为空");
        }
        if (scale < 0) {
            throw new IllegalArgumentException("scale 不能为负数");
        }
        amount = amount.setScale(scale, RoundingMode.HALF_EVEN);
    }

    // -- 工厂

    public static Money of(BigDecimal amount, String currencyCode, int scale) {
        return new Money(amount, currencyCode, scale);
    }

    public static Money of(String amount, String currencyCode, int scale) {
        return new Money(new BigDecimal(amount), currencyCode, scale);
    }

    public static Money of(BigDecimal amount, CurrencyInfo info) {
        return new Money(amount, info.getCurrencyCode(), info.getScale());
    }

    public static Money of(String amount, CurrencyInfo info) {
        return new Money(new BigDecimal(amount), info.getCurrencyCode(), info.getScale());
    }

    public static Money zero(String currencyCode, int scale) {
        return new Money(BigDecimal.ZERO, currencyCode, scale);
    }

    public static Money zero(CurrencyInfo info) {
        return zero(info.getCurrencyCode(), info.getScale());
    }

    /**
     * 按字符串解析金额，小数位需不超过 {@code info.getScale()}；
     * 超出精度时抛出 {@link IllegalArgumentException}（金额静默舍入属资损隐患，不吞不圆）
     */
    public static Money parse(String amount, CurrencyInfo info) {
        BigDecimal decimal = new BigDecimal(amount);
        int scale = info.getScale();
        if (decimal.scale() > scale) {
            throw new IllegalArgumentException(
                    "金额小数位超出币种精度: " + amount + "（" + info.getCurrencyCode() + " scale=" + scale + "）");
        }
        return of(decimal, info);
    }

    /**
     * 按字符串解析金额并显式舍入（HALF_EVEN）到币种精度
     */
    public static Money parseRounded(String amount, CurrencyInfo info) {
        return of(amount, info);
    }

    // -- 算术

    public Money add(Money other) {
        requireSameCurrency(other);
        return new Money(amount.add(other.amount), currencyCode, scale);
    }

    public Money subtract(Money other) {
        requireSameCurrency(other);
        return new Money(amount.subtract(other.amount), currencyCode, scale);
    }

    public Money multiply(BigDecimal factor) {
        Objects.requireNonNull(factor, "factor 不能为空");
        return new Money(amount.multiply(factor), currencyCode, scale);
    }

    public Money multiply(long factor) {
        return multiply(BigDecimal.valueOf(factor));
    }

    public Money divide(BigDecimal divisor) {
        Objects.requireNonNull(divisor, "divisor 不能为空");
        if (divisor.signum() == 0) {
            throw new IllegalArgumentException("divisor 不能为 0");
        }
        return new Money(amount.divide(divisor, scale, RoundingMode.HALF_EVEN), currencyCode, scale);
    }

    public Money negate() {
        return new Money(amount.negate(), currencyCode, scale);
    }

    public Money abs() {
        return new Money(amount.abs(), currencyCode, scale);
    }

    /**
     * 调整精度，返回新 {@link Money}
     */
    public Money withScale(int newScale) {
        return new Money(amount, currencyCode, newScale);
    }

    public boolean isZero() {
        return amount.signum() == 0;
    }

    public boolean isPositive() {
        return amount.signum() > 0;
    }

    public boolean isNegative() {
        return amount.signum() < 0;
    }

    /**
     * 全序比较，满足 {@link Comparable} 契约（混币集合可安全 sort / TreeSet）：
     * 先按币种代码字典序，再按金额；金额比较忽略 scale 差异（{@code 1.0 == 1.00}）。
     * <p>注意：与 record 自动生成的 {@code equals}（含 scale）不一致，{@code compareTo == 0} 不蕴含 {@code equals}；
     * 需要「同币种金额比较」的业务场景请使用 {@link #compareAmount(Money)}</p>
     */
    @Override
    public int compareTo(@NonNull Money other) {
        Objects.requireNonNull(other, "other 不能为空");
        int byCurrency = currencyCode.compareTo(other.currencyCode);
        if (byCurrency != 0) {
            return byCurrency;
        }
        return amount.compareTo(other.amount);
    }

    /**
     * 同币种金额比较；币种不一致抛 {@link IllegalArgumentException}
     */
    public int compareAmount(@NonNull Money other) {
        requireSameCurrency(other);
        return amount.compareTo(other.amount);
    }

    private void requireSameCurrency(Money other) {
        Objects.requireNonNull(other, "other 不能为空");
        if (!currencyCode.equals(other.currencyCode)) {
            throw new IllegalArgumentException(
                    "币种不一致，无法运算: " + currencyCode + " vs " + other.currencyCode);
        }
    }
}
