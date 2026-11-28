package cc.uncarbon.framework.helium.db.fieldcrypto.annotation;

import java.lang.annotation.*;

/**
 * 标注在实体的「敏感字段」上
 *
 * @author Uncarbon
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TableFieldCrypto {

    /**
     * 列路径，约定取值：表名.字段名
     * <P>如果不填，则由实体类上的 {@code @TableName} + {@code @TableField} 反射自动派生
     * <P>仅当派生值不合意（如实体与物理表名不一致）时才覆盖
     * <P>一经正式使用禁止变更，否则无法解密
     */
    String columnPath() default "";
}
