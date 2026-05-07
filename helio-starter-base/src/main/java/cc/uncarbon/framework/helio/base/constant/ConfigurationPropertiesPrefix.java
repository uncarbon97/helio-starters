package cc.uncarbon.framework.helio.base.constant;

import lombok.experimental.UtilityClass;

/**
 * 用于 @ConfigurationProperties 注解 prefix 字段的常量
 *
 * @author Uncarbon
 */
@UtilityClass
public final class ConfigurationPropertiesPrefix {

    public static final String BASE = "helio.base";
    public static final String DB_IDGEN = "helio.db.idgen";
    public static final String DB_MYBATIS_PLUS = "helio.db.mybatis-plus";
    public static final String JACKSON = "helio.jackson";
    public static final String SA_TOKEN = "helio.satoken";
    public static final String TENANT = "helio.tenant";
    public static final String WEB = "helio.web";
    public static final String WEB_SECURITY = "helio.web-security";
}
