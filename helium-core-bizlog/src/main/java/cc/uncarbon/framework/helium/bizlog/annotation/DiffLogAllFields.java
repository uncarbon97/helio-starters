package cc.uncarbon.framework.helium.bizlog.annotation;

import java.lang.annotation.*;

/**
 * 全字段参与 diff
 * <p>
 * 类级别注解，标注后该类中所有未单独标注的字段均参与对象差异比对；
 * 如需排除个别字段，可在对应字段上使用 {@link DiffLogIgnore}。
 *
 * @author wulang@mzt-biz-log
 * @author Uncarbon
 **/
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface DiffLogAllFields {
}
