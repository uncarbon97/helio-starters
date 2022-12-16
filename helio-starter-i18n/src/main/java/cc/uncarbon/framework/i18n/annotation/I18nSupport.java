package cc.uncarbon.framework.i18n.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface I18nSupport {

    /**
     * i18n/message.properties 中的消息代码
     * 如果不填写、并且标注在枚举上，则会尝试以枚举名获取对应国际化翻译值；如果
     *
     * The code in i18n/message.properties
     * If not filled in and marked on the enumeration, it will try to obtain the corresponding internationalized translation value with the enumeration name
     */
    @AliasFor(value = "value")
    String code() default "";

    @AliasFor(value = "code")
    String value() default "";

}
