package cc.uncarbon.framework.helio.i18n.context;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Locale;
import java.util.TimeZone;

/**
 * 国际化上下文
 *
 * @author Uncarbon
 */
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SimpleI18nContext implements I18nContext {

    // -- 多语言
    @Schema(description = "语言名称")
    protected String languageTag;

    @Schema(description = "Locale 对象实例")
    protected Locale locale;

    // -- 多时区

    /**
     * 例如：外显时刻按伦敦时间(UTC+0)，数据存储时刻按北京时间(UTC+8)，那么本字段就赋值为 -480
     */
    @Schema(description = "外显时刻与数据存储时刻的偏移量，单位=分钟")
    protected Integer timezoneOffset;

    @Schema(description = "TimeZone 对象实例")
    protected TimeZone timeZone;

}
