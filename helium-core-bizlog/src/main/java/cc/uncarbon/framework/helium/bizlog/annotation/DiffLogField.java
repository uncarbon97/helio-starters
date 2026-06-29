package cc.uncarbon.framework.helium.bizlog.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 参与 diff 的字段
 * <p>
 * 标注在字段上，指定其在差异文案中的显示名称，以及可选的值转换函数（对应一个 {@code IParseFunction}）。
 *
 * @author mzt@mzt-biz-log
 * @author Uncarbon
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface DiffLogField {

    /**
     * @return 字段在日志中显示的中文名（必填）
     */
    String name();

    /**
     * @return 自定义函数名，用于把字段值做翻译（如 SEX、ORDER）；留空则原值直接拼入日志
     */
    String function() default "";

    //   String dateFormat() default "";
}
