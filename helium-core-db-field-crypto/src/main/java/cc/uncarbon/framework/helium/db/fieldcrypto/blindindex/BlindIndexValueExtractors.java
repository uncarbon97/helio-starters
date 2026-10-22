package cc.uncarbon.framework.helium.db.fieldcrypto.blindindex;

/**
 * 内置盲索引取值提取器
 *
 * @author Uncarbon
 */
public final class BlindIndexValueExtractors {
    private BlindIndexValueExtractors() {
    }

    /**
     * 原样使用（仅去首尾空白）
     */
    public static class Identity implements BlindIndexValueExtractor {
        @Override
        public String extract(String rawValue) {
            return rawValue == null ? null : rawValue.trim();
        }
    }

    /**
     * 去除全部空白（含内嵌空格，方案 §3.3 手机号归一化口径：{@code 139 0000 0000} -&gt; {@code 13900000000}）
     */
    public static class CleanTrim extends Identity {
        @Override
        public String extract(String rawValue) {
            String trimmed = super.extract(rawValue);
            return trimmed == null ? null : trimmed.replaceAll("\\s+", "");
        }
    }

    /**
     * 小写归一化（适用于邮箱等大小写不敏感等值查询；方案 §3.3：trim + 小写，不做 Gmail 点号/+别名折叠）
     */
    public static class LowerTrim extends Identity {
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
     */
    public static class TailDigits implements BlindIndexValueExtractor {

        private final int tailLength;

        public TailDigits(int tailLength) {
            this.tailLength = tailLength;
        }

        public static TailDigits of8() {
            return new TailDigits(8);
        }

        public static TailDigits of(int tailLength) {
            return new TailDigits(tailLength);
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
}
