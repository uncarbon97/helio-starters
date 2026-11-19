package cc.uncarbon.framework.helium.excel.exporter;

import cc.uncarbon.framework.helium.base.errorcode.StructuredErrorCode;
import cc.uncarbon.framework.helium.base.exception.BusinessException;
import cc.uncarbon.framework.helium.excel.util.ExcelIoUtil;
import org.apache.fesod.sheet.ExcelWriter;
import org.apache.fesod.sheet.write.metadata.WriteSheet;

import java.io.OutputStream;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.LongSupplier;

/**
 * 后台管理批量导出通用模板
 * <p>固定流程：先计数守卫（超限直接失败，避免产出半截文件），再游标翻页按批写入</p>
 * <p>微服务化时，只需将 batchFetcher 换成远端游标翻页客户端，本模板与控制器侧模板均不变</p>
 */
public final class ExcelBatchExporter {

    private ExcelBatchExporter() {
    }


    /**
     * 一批数据 + 下一页游标
     *
     * @param rows       本批记录（非空）
     * @param nextCursor 下一页游标；返回行数满 batchSize 时必须非空
     */
    public record ExportBatch<T>(List<T> rows, Long nextCursor) {

        /**
         * 便捷构造
         */
        public static <T> ExportBatch<T> of(List<T> rows, Long nextCursor) {
            return new ExportBatch<>(rows, nextCursor);
        }

        public boolean isEmpty() {
            return rows == null || rows.isEmpty();
        }
    }


    /**
     * 游标翻页取批
     *
     * @param <T> 记录类型
     */
    @FunctionalInterface
    public interface BatchFetcher<T> {

        /**
         * 按游标取下一批
         *
         * @param cursor 上一批游标；首页为 null
         * @return 一批数据；空批表示取数结束
         */
        ExportBatch<T> fetch(Long cursor);
    }


    /**
     * 通用导出流程
     *
     * @param out                目标输出流（由调用方负责生命周期）
     * @param sheetName          Sheet 名
     * @param head               表头（与 rowMapper 产出的列序严格一致）
     * @param maxRows            导出行数上限
     * @param batchSize          单批行数；返回行数不足时视为最后一批
     * @param overLimitErrorCode 超限错误码（消息模板以 {} 接收 maxRows）
     * @param countFn            总数查询（用于超限守卫）
     * @param batchFetcher       游标翻页取批
     * @param rowMapper          单条记录转一行
     */
    public static <T> void export(OutputStream out, String sheetName, List<List<String>> head,
                                  long maxRows, int batchSize, StructuredErrorCode overLimitErrorCode,
                                  LongSupplier countFn, BatchFetcher<T> batchFetcher,
                                  Function<T, List<Object>> rowMapper) {
        long total = countFn.getAsLong();
        if (total > maxRows) {
            throw new BusinessException(overLimitErrorCode, maxRows);
        }

        ExcelWriter writer = ExcelIoUtil.newWriter(out);
        try {
            WriteSheet sheet = ExcelIoUtil.newSheet(0, sheetName, head);
            // 先落表头，空结果也能产出仅表头的文件
            writer.write(List.of(), sheet);

            Long cursor = null;
            while (true) {
                ExportBatch<T> batch = batchFetcher.fetch(cursor);
                if (batch == null || batch.isEmpty()) {
                    break;
                }
                writer.write(batch.rows().stream().map(rowMapper).toList(), sheet);
                if (batch.rows().size() < batchSize) {
                    break;
                }
                cursor = batch.nextCursor();
                if (cursor == null) {
                    // 防御：满批但领域侧未给出游标，视为结束
                    break;
                }
            }
        } finally {
            ExcelIoUtil.finish(writer);
        }
    }


    /**
     * 一页 ExcelRow + 总数 + 下页游标（配合 {@link #exportByRow} 使用）
     *
     * @param rows       本页记录
     * @param total      首页总数（用于超限守卫，仅首页取数时必填，续页可填 0）
     * @param nextCursor 下一页游标；首页行数满批且还有数据时必须非空
     */
    public record PageBatch<B>(List<B> rows, long total, Long nextCursor) {

        /**
         * 便捷构造
         */
        public static <B> PageBatch<B> of(List<B> rows, long total, Long nextCursor) {
            return new PageBatch<>(rows, total, nextCursor);
        }

        public boolean isEmpty() {
            return rows == null || rows.isEmpty();
        }
    }


    /**
     * ExcelRow 类驱动的通用导出流程：表头与写行均取自 rowClass 上的 {@code @ExcelProperty} 注解
     * <p>先取首页（首页 count 兼作超限守卫，超限直接失败零字节），再游标续批按批写入；
     * 典型取数闭包：复用 service 既有分页方法，往查询参数塞 {@code CursorPageParam(lastId=cursor)} 即可</p>
     *
     * @param out                目标输出流（由调用方负责生命周期）
     * @param sheetName          Sheet 名
     * @param rowClass           ExcelRow 类（字段标注 {@code @ExcelProperty}）
     * @param excludeFieldNames  排除字段名（可选列）；null/空 = 全列
     * @param maxRows            导出行数上限
     * @param overLimitErrorCode 超限错误码（消息模板以 {} 接收 maxRows）
     * @param pageFetcher        cursor（null=首页）-> 一页 ExcelRow + 总数 + 下页游标
     */
    public static <B> void exportByRow(OutputStream out, String sheetName, Class<B> rowClass,
                                       Set<String> excludeFieldNames, long maxRows,
                                       StructuredErrorCode overLimitErrorCode,
                                       Function<Long, PageBatch<B>> pageFetcher) {
        List<ExcelIoUtil.ColMeta> cols = ExcelIoUtil.declaredHead(rowClass).stream()
                .filter(col -> excludeFieldNames == null || excludeFieldNames.isEmpty()
                        || !excludeFieldNames.contains(col.fieldName()))
                .toList();

        // 首页先取数：count 兼作超限守卫，超限在落任何字节前失败
        PageBatch<B> first = pageFetcher.apply(null);
        if (first != null && first.total() > maxRows) {
            throw new BusinessException(overLimitErrorCode, maxRows);
        }

        ExcelWriter writer = ExcelIoUtil.newWriter(out);
        try {
            WriteSheet sheet = ExcelIoUtil.newSheet(0, sheetName, ExcelIoUtil.headOf(cols));
            // 先落表头，空结果也能产出仅表头的文件
            writer.write(List.of(), sheet);
            if (first == null || first.isEmpty()) {
                return;
            }

            writer.write(first.rows().stream().map(row -> ExcelIoUtil.rowValues(row, cols)).toList(), sheet);
            // 以首页行数为满批基准：续批行数不足即视为最后一页
            int batchSize = first.rows().size();
            Long cursor = first.nextCursor();
            while (cursor != null) {
                PageBatch<B> batch = pageFetcher.apply(cursor);
                if (batch == null || batch.isEmpty()) {
                    break;
                }
                writer.write(batch.rows().stream().map(row -> ExcelIoUtil.rowValues(row, cols)).toList(), sheet);
                if (batch.rows().size() < batchSize) {
                    break;
                }
                cursor = batch.nextCursor();
            }
        } finally {
            ExcelIoUtil.finish(writer);
        }
    }
}
