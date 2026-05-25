package cc.uncarbon.framework.helium.base.constant;

import lombok.experimental.UtilityClass;

/**
 * 用于 @ConfigurationProperties 注解 prefix 字段的常量
 *
 * @author Uncarbon
 */
@UtilityClass
public final class ConfigurationPropertiesPrefix {

    public static final String FRAMEWORK_NAME = "helium";
    public static final String BASE = FRAMEWORK_NAME + ".base";
    public static final String DB_IDGEN = FRAMEWORK_NAME + ".db.idgen";
    public static final String DB_MYBATIS_PLUS = FRAMEWORK_NAME + ".db.mybatis-plus";
    public static final String I18N = FRAMEWORK_NAME + ".i18n";
    public static final String JACKSON = FRAMEWORK_NAME + ".jackson";
    public static final String OPENAPI3 = FRAMEWORK_NAME + ".openapi3";
    public static final String SA_TOKEN = FRAMEWORK_NAME + ".satoken";
    public static final String TENANT = FRAMEWORK_NAME + ".tenant";
    public static final String WEB = FRAMEWORK_NAME + ".web";
    public static final String WEB_SECURITY = FRAMEWORK_NAME + ".web-security";
}
