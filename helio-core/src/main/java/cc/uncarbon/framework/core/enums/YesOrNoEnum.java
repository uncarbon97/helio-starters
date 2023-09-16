package cc.uncarbon.framework.core.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 是或否枚举
 */
@AllArgsConstructor
@Getter
public enum YesOrNoEnum implements HelioBaseEnum<Integer> {

    NO(0, "否"),
    YES(1, "是"),

    ;
    @EnumValue
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
     * 取反值
     * @param old 原值
     * @return 新值
     */
    public static YesOrNoEnum reverse(YesOrNoEnum old) {
        if (old == null) {
            return null;
        }

        switch (old) {
            case YES:
                return NO;
            case NO:
                return YES;
        }

        throw new IllegalArgumentException();
    }
}
