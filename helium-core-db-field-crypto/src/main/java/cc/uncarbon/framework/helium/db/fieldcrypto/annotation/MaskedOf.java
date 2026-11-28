package cc.uncarbon.framework.helium.db.fieldcrypto.annotation;

import cc.uncarbon.framework.helium.db.fieldcrypto.field.FieldValueMasker;

import java.lang.annotation.*;

/**
 * 标注在实体的「脱敏冗余字段」上，声明其派生来源
 * <p>同一敏感字段，可同时挂多个 {@link BlindIndexOf} + 一个 {@link MaskedOf}；
 * 清空必须把所有派生字段，一并置为 NULL
 *
 * @author Uncarbon
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MaskedOf {

    /**
     * 敏感字段名
     */
    String value();

    /**
     * 脱敏器实现，需具备 public 无参构造；实现留在业务侧
     */
    Class<? extends FieldValueMasker> masker();
}
