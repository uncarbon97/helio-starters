package cc.uncarbon.framework.helio.db.enums;

import cc.uncarbon.framework.helio.base.enums.BaseEnum;
import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 是或否枚举
 */
@AllArgsConstructor
@Getter
public enum YesOrNoEnum implements BaseEnum<Integer> {

    NO(0, "否"),
    YES(1, "是"),

    ;@EnumValue
    private final Integer value;
    private final String label;

    /**
     * 根据值得到枚举对象
     * @param value 外部值
     * @return null or 枚举对象
     */
    public static YesOrNoEnum of(Integer value) {
        if (value == null) {
            return null;
        }

        // 区分度小，直接用if判断了
        if (YES.value.equals(value)) {
            return YES;
        }
        if (NO.value.equals(value)) {
            return NO;
        }
        return null;
    }

    /**
     * 根据值得到枚举对象
     * @param value 外部值
     * @return null or 枚举对象
     */
    public static YesOrNoEnum of(boolean value) {
        return value ? YES : NO;
    }
}
