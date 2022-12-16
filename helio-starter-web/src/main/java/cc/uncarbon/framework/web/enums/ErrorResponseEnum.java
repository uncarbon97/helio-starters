package cc.uncarbon.framework.web.enums;


import cc.uncarbon.framework.core.enums.HelioBaseEnum;
import cc.uncarbon.framework.i18n.annotation.I18nSupport;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorResponseEnum implements HelioBaseEnum<Integer> {

    @I18nSupport
    ILLEGAL_ENUM_VALUE(400, "非法枚举值"),

    @I18nSupport
    CONTAINS_ILLEGAL_CHARACTER(400, "包含非法字符"),

    ;

    private final Integer value;
    private final String label;
}
