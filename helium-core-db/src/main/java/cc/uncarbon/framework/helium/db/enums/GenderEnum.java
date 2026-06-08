package cc.uncarbon.framework.helium.db.enums;

import cc.uncarbon.framework.helium.base.enums.BaseEnum;
import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * 生理性别枚举
 */
@AllArgsConstructor
@Getter
public enum GenderEnum implements BaseEnum<Integer> {

    UNKNOWN(0, "未知"),
    MALE(1, "男"),
    FEMALE(2, "女"),

    ;@EnumValue
    private final Integer value;
    private final String label;

}
