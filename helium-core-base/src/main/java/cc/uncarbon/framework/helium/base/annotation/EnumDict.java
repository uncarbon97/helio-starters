package cc.uncarbon.framework.helium.base.annotation;

/**
 * 标记枚举类为字典枚举类
 *
 * @author Uncarbon
 */
public @interface EnumDict {

    /**
     * 字典分类编码
     */
    String value() default "";

    /**
     * 是否在「后台管理-字典管理」中显示
     */
    boolean displayOnAdmin() default true;

}
