package cc.uncarbon.framework.tenant.annotation;

import cc.uncarbon.framework.tenant.config.GlobalTenantDataSourceConfiguration;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

/**
 * 启用基于全局 AOP 的数据源级多租户
 *
 * @author Uncarbon
 */
@Import(value = GlobalTenantDataSourceConfiguration.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnabledGlobalTenantDataSource {

}
