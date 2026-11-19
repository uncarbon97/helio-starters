package cc.uncarbon.framework.helium.excel.converter;

import org.apache.fesod.sheet.converters.Converter;
import org.apache.fesod.sheet.enums.CellDataTypeEnum;
import org.apache.fesod.sheet.metadata.GlobalConfiguration;
import org.apache.fesod.sheet.metadata.data.WriteCellData;
import org.apache.fesod.sheet.metadata.property.ExcelContentProperty;

import java.math.BigDecimal;

/**
 * 大数值写出转换器：超过 15 位的 Long 走字符串单元格
 */
public class BigNumberStringConverter implements Converter<Long> {

    /**
     * Excel 数值安全位数上限（double 精确整数边界以内）
     */
    private static final int SAFE_DIGITS = 15;

    @Override
    public Class<Long> supportJavaTypeKey() {
        return Long.class;
    }

    /**
     * 返回 null 以占用写端 {@code (Long, null)} 转换器槽位
     */
    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return null;
    }

    @Override
    public WriteCellData<?> convertToExcelData(Long value, ExcelContentProperty contentProperty,
                                               GlobalConfiguration globalConfiguration) {
        if (value != null) {
            String str = Long.toString(value);
            if (str.length() > SAFE_DIGITS) {
                return new WriteCellData<>(str);
            }
            WriteCellData<?> data = new WriteCellData<>(BigDecimal.valueOf(value));
            data.setType(CellDataTypeEnum.NUMBER);
            return data;
        }
        return new WriteCellData<>(CellDataTypeEnum.EMPTY);
    }
}
