package cc.uncarbon.framework.crud.enums;

import cc.uncarbon.framework.core.enums.HelioBaseEnum;
import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum YesOrNoEnum implements HelioBaseEnum<Integer> {

    YES(1, "是"),

    NO(0, "否"),

    ;

    @EnumValue
    private final Integer value;
    private final String label;

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
