package cc.uncarbon.framework.helium.i18n.context;

import cc.uncarbon.framework.helium.i18n.context.currency.CurrencyInfo;
import cc.uncarbon.framework.helium.i18n.context.lang.LangInfo;
import cc.uncarbon.framework.helium.i18n.context.timezone.TimezoneInfo;
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

    @Schema(description = "多币种信息")
    protected CurrencyInfo currencyInfo;

}
