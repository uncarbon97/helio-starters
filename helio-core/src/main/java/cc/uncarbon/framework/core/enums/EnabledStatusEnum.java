package cc.uncarbon.framework.core.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * 启用状态枚举
 */
@AllArgsConstructor
@Getter
public enum EnabledStatusEnum implements HelioBaseEnum<Integer> {

    DISABLED(0, "禁用"),
    ENABLED(1, "启用"),

    ;
    @EnumValue
    private final Integer value;
    private final String label;

    /**
     * 根据值得到枚举对象
     * @param value 外部值
     * @return null or 枚举对象
     */
    public static EnabledStatusEnum of(Integer value) {
        if (value == null) {
            return null;
        }

        // 区分度小，直接用if判断了
        if (DISABLED.value.equals(value)) {
            return DISABLED;
        }
        if (ENABLED.value.equals(value)) {
            return ENABLED;
        }
        return null;
    }

    /**
     * 根据值得到枚举对象
     * @param value 外部值
     * @return null or 枚举对象
     */
    public static EnabledStatusEnum of(boolean value) {
        return value ? ENABLED : DISABLED;
    }

    /**
     * 取反值
     * @param old 原值
     * @return 新值
     */
    public static EnabledStatusEnum reverse(EnabledStatusEnum old) {
        if (old == null) {
            return null;
        }

        switch (old) {
            case DISABLED:
                return ENABLED;
            case ENABLED:
                return DISABLED;
        }

        throw new IllegalArgumentException();
    }
}
