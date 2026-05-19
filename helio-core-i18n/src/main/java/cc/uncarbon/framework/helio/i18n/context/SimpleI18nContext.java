package cc.uncarbon.framework.helio.i18n.context;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 简单国际化上下文
 *
 * @author Uncarbon
 */
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SimpleI18nContext implements I18nContext {


    @Schema(description = "多语言信息")
    protected LangInfo langInfo;

    @Schema(description = "多时区信息")
    protected TimezoneInfo timezoneInfo;

}
