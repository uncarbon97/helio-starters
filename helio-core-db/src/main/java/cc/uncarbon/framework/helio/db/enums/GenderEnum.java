package cc.uncarbon.framework.helio.db.enums;

import cc.uncarbon.framework.helio.base.enums.BaseEnum;
import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * 生理性别枚举类
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
