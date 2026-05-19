package cc.uncarbon.framework.helio.i18n.context;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Locale;

/**
 * 简单多语言信息
 *
 * @author Uncarbon
 */
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
public class SimpleLangInfo implements LangInfo {


    @Schema(description = "符合 IETF BCP 47 的语言标签", example = "zh-CN, zh-TW")
    protected String languageTag;

    @Schema(description = "Locale 对象实例")
    protected Locale locale;

}
