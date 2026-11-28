package cc.uncarbon.framework.helium.tenant.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注在类、方法上：作用域内忽略租户；
 * 标注在实体类上：对应数据表不参与租户隔离
 *
 * @author Uncarbon
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TenantIgnore {

}
