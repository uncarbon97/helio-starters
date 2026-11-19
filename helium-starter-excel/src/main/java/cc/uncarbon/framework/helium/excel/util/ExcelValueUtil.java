package cc.uncarbon.framework.helium.excel.util;

import cc.uncarbon.framework.helium.base.enums.BaseEnum;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.StrUtil;
import lombok.experimental.UtilityClass;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Excel 导出取值工具类
 */
@UtilityClass
public final class ExcelValueUtil {

    /**
     * 单元格文本安全上限（Excel 硬限制）
     */
    public static final int CELL_TEXT_MAX_LENGTH = 32767;

    /**
     * 通用拷贝选项：关闭默认类型转换、字段值统一按单元格规则加工
     *
     * @param instantFormatter {@link Instant} 类型字段使用的格式化器
     */
    public static CopyOptions defaultCopyOptions(@NonNull DateTimeFormatter instantFormatter) {
        return CopyOptions.create()
                // 关闭默认类型转换，交给 FieldValueEditor 处理
                .setConverter((type, value) -> value)
                .setFieldValueEditor((field, value) ->
                        // 枚举列取外显标签
                        value instanceof BaseEnum<?> dict ? labelOf(dict)
                        // 其余文本列统一清洗
                                : value instanceof String text ? clean(text)
                                : value instanceof Instant instant ? instantFormatter.format(instant)
                                : value);
    }

    /**
     * 枚举列取外显标签；null 安全（输出空串，保持单元格存在）
     *
     * @param dict 实现了 {@link BaseEnum} 的枚举值
     * @return 标签文本；入参为 null 时返回空串
     */
    public static String labelOf(@Nullable BaseEnum<?> dict) {
        return dict == null ? StrUtil.EMPTY : dict.getLabel();
    }

    /**
     * 清洗控制字符 + 截断 + 防公式注入
     * 长度上限 {@link CELL_TEXT_MAX_LENGTH}
     *
     * @param raw 原始文本
     */
    public static String clean(@Nullable String raw) {
        return clean(raw, CELL_TEXT_MAX_LENGTH);
    }

    /**
     * 清洗控制字符 + 截断 + 防公式注入
     *
     * @param raw       原始文本
     * @param maxLength 保留的最大长度（按字符计，截断发生在清洗控制字符之后）
     */
    public static String clean(@Nullable String raw, int maxLength) {
        if (raw == null || raw.isEmpty()) {
            return raw;
        }
        String cleaned = stripControlChars(raw);
        if (cleaned.length() > maxLength) {
            cleaned = cleaned.substring(0, maxLength);
        }
        char first = cleaned.charAt(0);
        if (first == '=' || first == '+' || first == '-' || first == '@') {
            return "'" + cleaned;
        }
        return cleaned;
    }

    /**
     * 控制字符（0x00-0x1F、0x7F）替换为空格，避免单元格乱码
     */
    private static String stripControlChars(String raw) {
        StringBuilder sb = new StringBuilder(raw.length());
        raw.chars().forEachOrdered(c -> sb.append(c < 0x20 || c == 0x7F ? ' ' : (char) c));
        return sb.toString();
    }

}
