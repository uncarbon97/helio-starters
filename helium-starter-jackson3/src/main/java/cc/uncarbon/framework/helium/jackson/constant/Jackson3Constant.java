package cc.uncarbon.framework.helium.jackson.constant;

import cn.hutool.core.date.DatePattern;

import java.time.format.DateTimeFormatter;

public class Jackson3Constant {

    public static final DateTimeFormatter UTC_WITH_XXX_OFFSET_FORMATTER
            = DateTimeFormatter.ofPattern(DatePattern.UTC_WITH_XXX_OFFSET_PATTERN);
}
