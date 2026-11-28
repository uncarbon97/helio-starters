package cc.uncarbon.framework.helium.excel.util;

import cc.uncarbon.framework.helium.base.errorcode.StructuredErrorCode;
import cc.uncarbon.framework.helium.base.exception.BusinessException;
import cc.uncarbon.framework.helium.excel.converter.BigNumberStringConverter;
import cc.uncarbon.framework.helium.excel.model.ExcelRow;
import cc.uncarbon.framework.helium.excel.style.AdaptiveColumnWidthStyleStrategy;
import cn.hutool.core.text.CharSequenceUtil;
import lombok.experimental.UtilityClass;
import org.apache.fesod.sheet.ExcelWriter;
import org.apache.fesod.sheet.FesodSheet;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.apache.fesod.sheet.context.AnalysisContext;
import org.apache.fesod.sheet.event.AnalysisEventListener;
import org.apache.fesod.sheet.exception.ExcelAnalysisException;
import org.apache.fesod.sheet.write.metadata.WriteSheet;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Excel 读写薄封装（基于 Apache Fesod）
 * <p>读取走 SAX 逐行回调，整文件不驻留堆内存；写入按批追加</p>
 */
@UtilityClass
public final class ExcelIoUtil {

    /**
     * xlsx（zip 容器）魔数
     */
    private static final byte[] XLSX_MAGIC = {'P', 'K', 0x03, 0x04};


    /**
     * SAX 逐行解析第一个 Sheet（首行为表头）
     *
     * @param in                 输入流（调用方负责关闭）
     * @param maxDataRows        数据行上限，超出直接失败（防止大文件 OOM）
     * @param overLimitErrorCode 数据行数超上限时抛出的错误码（由调用方注入，框架层不持有业务错误码）
     * @param parseFailErrorCode 文件非法或解析失败时抛出的错误码
     */
    public static ParsedSheet parse(InputStream in, int maxDataRows,
                                    StructuredErrorCode overLimitErrorCode,
                                    StructuredErrorCode parseFailErrorCode) {
        InputStream guarded = requireXlsxMagic(in, parseFailErrorCode);
        Map<Integer, String> headerByIndex = new TreeMap<>();
        List<ParsedRow> rows = new ArrayList<>();
        try {
            FesodSheet.read(guarded, new AnalysisEventListener<Map<Integer, String>>() {

                int dataRowIndex = 0;

                @Override
                public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
                    headMap.forEach((idx, name) -> headerByIndex.put(idx, name == null ? "" : name.trim()));
                }

                @Override
                public void invoke(Map<Integer, String> row, AnalysisContext context) {
                    if (rows.size() >= maxDataRows) {
                        // 快速失败，不继续解析
                        throw new BusinessException(overLimitErrorCode, maxDataRows);
                    }
                    rows.add(new ParsedRow(2 + dataRowIndex, new LinkedHashMap<>(row)));
                    dataRowIndex++;
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    // 无事可做
                }
            }).sheet(0).headRowNumber(1).doRead();
        } catch (BusinessException e) {
            // 监听器内抛出的业务异常（如行数超限）原样上抛，避免被误报为解析失败
            throw e;
        } catch (ExcelAnalysisException e) {
            if (e.getCause() instanceof BusinessException be) {
                throw be;
            }
            throw new BusinessException(parseFailErrorCode);
        } catch (RuntimeException e) {
            throw new BusinessException(parseFailErrorCode);
        }
        return new ParsedSheet(headerByIndex, rows);
    }

    /**
     * 新建写入器（调用方负责 {@link #finish(ExcelWriter)}）
     */
    public static ExcelWriter newWriter(OutputStream out) {
        return FesodSheet.write(out)
                // CJK 感知自适应列宽（替代原定宽 24）
                .registerWriteHandler(new AdaptiveColumnWidthStyleStrategy())
                .build();
    }

    /**
     * 新建 Sheet，携带动态表头
     */
    public static WriteSheet newSheet(int sheetNo, String sheetName, List<List<String>> head) {
        return FesodSheet.writerSheet(sheetNo, sheetName)
                .head(head)
                // 大数走字符串，规避 xlsx double 精度截断；须注册在 Sheet 级以占用 (Long, null) 写出槽位
                .registerConverter(new BigNumberStringConverter())
                .build();
    }

    /**
     * 新建 Sheet，表头取自 ExcelRow 类上的 {@link ExcelProperty} 声明（走动态表头路由，不启用 fesod 类头机器）
     *
     * @param excludeFieldNames 需排除的字段名（可选列）；null/空 = 全列
     */
    public static WriteSheet newSheet(int sheetNo, String sheetName, Class<?> rowClass, Set<String> excludeFieldNames) {
        List<ColMeta> cols = declaredHead(rowClass).stream()
                .filter(col -> excludeFieldNames == null || excludeFieldNames.isEmpty()
                        || !excludeFieldNames.contains(col.fieldName()))
                .toList();
        return newSheet(sheetNo, sheetName, headOf(cols));
    }

    /**
     * 解析为 ExcelRow 对象列表：先走既有 SAX Map 路径（魔数校验、行数上限快速失败、单元格文本归一化全部复用），
     * 再按「表头名 → {@link ExcelProperty} 字段」反射填充；字段值一律为归一化后的字符串文本，
     * 类型转换与语义校验（jakarta.validation 等）留给业务侧
     *
     * @param rowClass           ExcelRow 类，须有公共无参构造器，字段须为可写 String
     * @param overLimitErrorCode 数据行数超上限时抛出的错误码（透传给 {@link #parse(InputStream, int, StructuredErrorCode, StructuredErrorCode)}）
     * @param parseFailErrorCode 文件非法或解析失败时抛出的错误码
     */
    public static <B extends ExcelRow> ParsedRows<B> parse(InputStream in, Class<B> rowClass, int maxRows,
                                                           StructuredErrorCode overLimitErrorCode, StructuredErrorCode parseFailErrorCode) {
        ParsedSheet sheet = parse(in, maxRows, overLimitErrorCode, parseFailErrorCode);
        List<ColMeta> cols = declaredHead(rowClass);
        Constructor<B> constructor;
        try {
            constructor = rowClass.getDeclaredConstructor();
            constructor.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("ExcelRow 类缺少无参构造器: " + rowClass.getName(), e);
        }

        // 表头名 -> 列序号（同名取最靠前，与 ParsedSheet.columnIndexOf 语义一致）
        Map<String, Integer> colIndexByHeadName = new HashMap<>();
        sheet.headerByIndex().forEach((idx, name) -> colIndexByHeadName.putIfAbsent(name, idx));

        List<RowData<B>> rows = new ArrayList<>(sheet.rows().size());
        for (ParsedRow row : sheet.rows()) {
            B bo;
            try {
                bo = constructor.newInstance();
                for (ColMeta col : cols) {
                    Integer idx = colIndexByHeadName.get(col.headName());
                    if (idx == null) {
                        continue;
                    }
                    col.field().set(bo, row.cell(idx));
                }
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("ExcelRow 反射填充失败: " + rowClass.getName(), e);
            }
            rows.add(new RowData<>(row.rowNumber(), bo));
        }
        return new ParsedRows<>(List.copyOf(colIndexByHeadName.keySet().stream()
                .filter(CharSequenceUtil::isNotBlank).toList()), rows);
    }

    /**
     * 读取 ExcelRow 类的列声明：标注 {@link ExcelProperty} 的非静态字段
     * <p>列序：显式设置 index 的字段按 index 升序在前，其余按字段声明序（getDeclaredFields 在 HotSpot 为声明序，
     * 跨 JVM 兼容要求高时建议显式设置 index）</p>
     */
    public static List<ColMeta> declaredHead(Class<?> rowClass) {
        Field[] fields = rowClass.getDeclaredFields();
        List<ColMeta> indexed = new ArrayList<>();
        List<ColMeta> declared = new ArrayList<>();
        for (Field field : fields) {
            ExcelProperty prop = field.getAnnotation(ExcelProperty.class);
            if (prop == null || Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            String headName = Arrays.stream(prop.value())
                    .filter(CharSequenceUtil::isNotBlank)
                    .findFirst()
                    .orElseGet(field::getName);
            ColMeta col = new ColMeta(field.getName(), headName.strip(), field, prop.index());
            field.setAccessible(true);
            if (prop.index() >= 0) {
                indexed.add(col);
            } else {
                declared.add(col);
            }
        }
        indexed.sort(Comparator.comparingInt(ColMeta::index));
        List<ColMeta> ret = new ArrayList<>(indexed.size() + declared.size());
        ret.addAll(indexed);
        ret.addAll(declared);
        return ret;
    }

    /**
     * 列声明转动态表头
     */
    public static List<List<String>> headOf(List<ColMeta> cols) {
        List<List<String>> head = new ArrayList<>(cols.size());
        for (ColMeta col : cols) {
            head.add(List.of(col.headName()));
        }
        return head;
    }

    /**
     * 按列声明顺序反射取一行字段值（导出写行用）
     */
    public static List<Object> rowValues(Object row, List<ColMeta> cols) {
        List<Object> values = new ArrayList<>(cols.size());
        try {
            for (ColMeta col : cols) {
                values.add(col.field().get(row));
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("ExcelRow 反射取值失败: " + row.getClass().getName(), e);
        }
        return values;
    }

    /**
     * 结束写入并刷盘
     */
    public static void finish(ExcelWriter writer) {
        if (writer != null) {
            writer.finish();
        }
    }

    /**
     * xlsx 为 zip 容器，先校验魔数，拦截扩展名伪造与明显损坏的文件
     */
    private static InputStream requireXlsxMagic(InputStream in, StructuredErrorCode parseFailErrorCode) {
        byte[] head = new byte[XLSX_MAGIC.length];
        try {
            int n = in.readNBytes(head, 0, head.length);
            if (n == head.length && Arrays.equals(head, XLSX_MAGIC)) {
                PushbackInputStream pb = new PushbackInputStream(in, head.length);
                pb.unread(head, 0, n);
                return pb;
            }
        } catch (Exception ignored) {
            // fallthrough
        }
        throw new BusinessException(parseFailErrorCode);
    }


    /**
     * ExcelRow 列声明元数据
     *
     * @param fieldName 字段名（排除可选列时使用）
     * @param headName  列头名（表头匹配、模板生成、导出表头三处同源）
     * @param field     反射字段（已 setAccessible）
     * @param index     显式列序；-1 = 未设置
     */
    public record ColMeta(String fieldName, String headName, Field field, int index) {
    }


    /**
     * 带行号的 ExcelRow 数据
     *
     * @param rowNumber Excel 行号（1-based，含表头行；首条数据行为 2）
     * @param row       行对象（字段值为归一化后的字符串文本）
     */
    public record RowData<B extends ExcelRow>(int rowNumber, B row) {
    }


    /**
     * ExcelRow 解析结果
     *
     * @param headNames 文件中出现的表头名（去空白，供业务侧与 {@link #declaredHead(Class)} 比对做表头匹配校验）
     * @param rows      带行号的数据行
     */
    public record ParsedRows<B extends ExcelRow>(List<String> headNames, List<RowData<B>> rows) {
    }


    /**
     * 解析结果
     *
     * @param headerByIndex 列序号 - 表头名（已 trim）
     * @param rows          数据行
     */
    public record ParsedSheet(Map<Integer, String> headerByIndex, List<ParsedRow> rows) {

        /**
         * 按表头名取列序号；不存在返回 -1；表头名重复时取最靠前
         */
        public int columnIndexOf(String name) {
            for (var e : headerByIndex.entrySet()) {
                if (name.equals(e.getValue())) {
                    return e.getKey();
                }
            }
            return -1;
        }

        /**
         * 是否包含全部给定表头
         */
        public boolean hasAllHeaders(Collection<String> names) {
            return names.stream().allMatch(name -> columnIndexOf(name) >= 0);
        }

    }


    /**
     * 单行数据
     *
     * @param rowNumber Excel 行号（1-based，含表头行；首条数据行为 2）
     * @param cells     列序号 - 原始单元格文本
     */
    public record ParsedRow(int rowNumber, Map<Integer, String> cells) {

        /**
         * 取单元格文本：去前后空白；纯数字尾部 ".0" 去除（Excel 数值单元格常见产物）；空白返回 null
         */
        public String cell(int colIndex) {
            String v = cells.get(colIndex);
            if (v == null) {
                return null;
            }
            v = v.strip();
            if (v.isEmpty()) {
                return null;
            }
            if (v.endsWith(".0") && v.length() > 2
                    && v.chars().limit(v.length() - 2).allMatch(Character::isDigit)) {
                v = v.substring(0, v.length() - 2);
            }
            return v;
        }

    }

}
