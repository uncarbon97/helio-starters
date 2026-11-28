package cc.uncarbon.framework.helium.jackson.constant;

import cn.hutool.core.date.DatePattern;
import lombok.experimental.UtilityClass;

import java.time.format.DateTimeFormatter;

@UtilityClass
public class Jackson3Constant {

    public final DateTimeFormatter UTC_WITH_XXX_OFFSET_FORMATTER
            = DateTimeFormatter.ofPattern(DatePattern.UTC_WITH_XXX_OFFSET_PATTERN);
}
