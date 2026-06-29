package cc.uncarbon.framework.helium.bizlog.annotation;

import java.lang.annotation.*;

/**
 * 忽略 diff 比对的字段
 * <p>
 * 标注在字段上，使其不参与对象差异比对。
 * <p>
 * 注意：注解名拼写为 {@code DIff}（D + I 双大写），为历史遗留，使用时务必照抄。
 *
 * @author wulang@mzt-biz-log
 * @author Uncarbon
 **/
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface DIffLogIgnore {
}
