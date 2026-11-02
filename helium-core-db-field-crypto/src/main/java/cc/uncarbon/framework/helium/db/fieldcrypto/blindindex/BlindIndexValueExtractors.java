package cc.uncarbon.framework.helium.db.fieldcrypto.blindindex;

/**
 * 内置盲索引取值提取器
 *
 * <p>通过 {@code @BlindIndexOf(extractor = ...)} 声明时，实现类需具备 public 无参构造；
 * 带参数的提取器（如 {@link TailDigits}）请使用预置子类（{@link Tail4Digits} 等）
 * 或配合 {@code @BlindIndexOf#extractorArg} 使用 {@code (int)} 构造</p>
 *
 * @author Uncarbon
 */
public final class BlindIndexValueExtractors {
    private BlindIndexValueExtractors() {
    }

    /**
     * 去首尾空白
     */
    public static class Trim implements BlindIndexValueExtractor {
        @Override
        public String extract(String rawValue) {
            return rawValue == null ? null : rawValue.trim();
        }
    }

    /**
     * 去除全部空白
     */
    public static class CleanTrim extends Trim {
        @Override
        public String extract(String rawValue) {
            String trimmed = super.extract(rawValue);
            return trimmed == null ? null : trimmed.replaceAll("\\s+", "");
        }
    }

    /**
     * 小写归一化 + 去首尾空白
     */
    public static class LowerTrim extends Trim {
        @Override
        public String extract(String rawValue) {
            String trimmed = super.extract(rawValue);
            return trimmed == null ? null : trimmed.toLowerCase();
        }
    }

    /**
     * 提取纯数字后取尾部 N 位（适用于手机号尾号检索）
     *
     * <p>数字位数不足时返回 null（放弃建立索引），避免短输入误建索引</p>
     * <p>注解声明时请使用 {@link Tail4Digits} / {@link Tail6Digits}，或指定 {@code extractorArg}</p>
     */
    public static class TailDigits implements BlindIndexValueExtractor {

        private final int tailLength;

        public TailDigits(int tailLength) {
            if (tailLength <= 0) {
                throw new IllegalArgumentException("tailLength 必须为正整数，实际: " + tailLength);
            }
            this.tailLength = tailLength;
        }

        public static TailDigits of(int tailLength) {
            return new TailDigits(tailLength);
        }

        public int getTailLength() {
            return tailLength;
        }

        @Override
        public String extract(String rawValue) {
            if (rawValue == null) {
                return null;
            }
            String digits = rawValue.replaceAll("\\D", "");
            return digits.length() >= tailLength
                    ? digits.substring(digits.length() - tailLength)
                    : null;
        }
    }

    /**
     * 尾 4 位数字（手机号尾号常用口径），可直接用于注解
     */
    public static class Tail4Digits extends TailDigits {
        public Tail4Digits() {
            super(4);
        }
    }

    /**
     * 尾 6 位数字，可直接用于注解
     */
    public static class Tail6Digits extends TailDigits {
        public Tail6Digits() {
            super(6);
        }
    }
}
