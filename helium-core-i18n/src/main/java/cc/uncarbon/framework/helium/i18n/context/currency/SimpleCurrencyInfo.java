package cc.uncarbon.framework.helium.i18n.context.currency;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 简单多币种信息
 *
 * @author Uncarbon
 */
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
public class SimpleCurrencyInfo implements CurrencyInfo {

    @Schema(description = "币种代码", example = "CNY、USD、USDT、BTC")
    protected String currencyCode;

    @Schema(description = "精度（小数位数）", example = "法币 2，USDT 6，BTC 8")
    protected int scale;

    @Schema(description = "货币符号", example = "¥、$、₮")
    protected String symbol;

    @Schema(description = "展示名称", example = "人民币、美元、泰达币")
    protected String displayName;

}
