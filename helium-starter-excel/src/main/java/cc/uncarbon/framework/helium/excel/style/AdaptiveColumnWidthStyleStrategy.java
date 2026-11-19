package cc.uncarbon.framework.helium.excel.style;

import org.apache.fesod.sheet.metadata.Head;
import org.apache.fesod.sheet.metadata.data.WriteCellData;
import org.apache.fesod.sheet.write.handler.context.CellWriteHandlerContext;
import org.apache.fesod.sheet.write.metadata.holder.WriteSheetHolder;
import org.apache.fesod.sheet.write.style.column.AbstractColumnWidthStyleStrategy;
import org.apache.fesod.sheet.write.style.column.LongestMatchColumnWidthStyleStrategy;
import org.apache.poi.ss.usermodel.Cell;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CJK 感知的自适应列宽策略
 * <p>按显示宽度计长：拉丁字符 1、CJK/全角等 2；逐列取表头与内容最大值，钳制在 [{@code 8}, {@code 50}]
 * <p>对比 EasyExcel 系内置 {@link LongestMatchColumnWidthStyleStrategy}（按字符数计），中文表头/内容不会被压成一半宽度
 * <p>对比定宽，窄列不再浪费，长文本有上限
 */
public class AdaptiveColumnWidthStyleStrategy extends AbstractColumnWidthStyleStrategy {

    private static final int DEFAULT_MIN_WIDTH = 8;

    private static final int DEFAULT_MAX_WIDTH = 50;

    /**
     * 单元格文本参与宽度计算的最大扫描长度（反正会被 max 截断）
     */
    private static final int SCAN_LIMIT = 128;

    private final int minWidth;

    private final int maxWidth;

    /**
     * sheetNo * SHIFT + 列序号 -&gt; 已见最大显示宽度
     */
    private final Map<Integer, Integer> widthByColumn = new HashMap<>();

    /**
     * 默认钳制 [{@value #DEFAULT_MIN_WIDTH}, {@value #DEFAULT_MAX_WIDTH}]
     */
    public AdaptiveColumnWidthStyleStrategy() {
        this(DEFAULT_MIN_WIDTH, DEFAULT_MAX_WIDTH);
    }

    public AdaptiveColumnWidthStyleStrategy(int minWidth, int maxWidth) {
        this.minWidth = minWidth;
        this.maxWidth = maxWidth;
    }

    @Override
    public void afterCellDispose(CellWriteHandlerContext context) {
        // 复用父类分发，保持与内置策略一致的生命周期语义
        super.afterCellDispose(context);
    }

    @Override
    protected void setColumnWidth(WriteSheetHolder writeSheetHolder, List<WriteCellData<?>> cellDataList,
                                  Cell cell, Head head, Integer relativeRowIndex, Boolean isHead) {
        if (cell == null) {
            return;
        }
        String text = cell.toString();
        if (text == null || text.isEmpty()) {
            return;
        }
        int width = Math.min(displayWidth(text, SCAN_LIMIT), maxWidth);
        int key = key(writeSheetHolder, cell);
        int current = widthByColumn.getOrDefault(key, 0);
        if (width <= current) {
            return;
        }
        widthByColumn.put(key, width);
        writeSheetHolder.getSheet().setColumnWidth(cell.getColumnIndex(), Math.clamp(width, minWidth, maxWidth) * 256);
    }

    /**
     * 显示宽度：CJK/全角区间（U+2E80 起）计 2，其余计 1
     */
    static int displayWidth(String text, int limit) {
        int width = 0;
        int counted = 0;
        for (int i = 0; i < text.length() && counted < limit; ) {
            int cp = text.codePointAt(i);
            i += Character.charCount(cp);
            width += cp >= 0x2E80 ? 2 : 1;
            counted++;
        }
        return width;
    }

    private static int key(WriteSheetHolder holder, Cell cell) {
        return holder.getSheetNo() * 100_000 + cell.getColumnIndex();
    }
}
