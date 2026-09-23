package cc.uncarbon.framework.helium.tenant.line;

import cc.uncarbon.framework.helium.base.condition.HeliumConditions;
import cc.uncarbon.framework.helium.tenant.enums.TenantIsolationStrategyEnum;
import cc.uncarbon.framework.helium.tenant.props.HeliumTenantProperties;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.type.AnnotatedTypeMetadata;

import javax.sql.DataSource;

/**
 * 基于行级多租户自动装配类
 *
 * @author Uncarbon
 */
@Conditional(value = TenantLineConfiguration.OnLineStrategy.class)
@Slf4j
public class TenantLineConfiguration {

    public static final String LOG_PREFIX = "[Framework][行级多租户]";

    /**
     * 基于 mybatis-plus 的行级租户拦截器
     */
    @Bean
    public TenantLineInnerInterceptor tenantLineInnerInterceptor(HeliumTenantProperties props) {
        log.info(LOG_PREFIX + " 已应用 >> 显式忽略的数据表: {}；participateByDefault={}（true=未显式忽略的表一律参与隔离）",
                props.getIgnoredTables(), props.isParticipateByDefault());
        return new TenantLineInnerInterceptor(new DefaultTenantLineHandler(props));
    }

    /**
     * 口径翻转（participateByDefault=true）时的启动期强对账：
     * 参与隔离的表缺 tenant_id 列则启动失败，把运行时炸提前到部署时
     */
    @Bean
    @Conditional(value = TenantLineConfiguration.OnParticipateByDefault.class)
    public TenantLineFieldReconciler tenantSchemaReconciler(DataSource dataSource, HeliumTenantProperties props) {
        return new TenantLineFieldReconciler(dataSource, props);
    }

    protected static class OnLineStrategy implements Condition {
        @Override
        public boolean matches(ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {
            var props = HeliumConditions.bind(context.getEnvironment(), HeliumTenantProperties.class);
            return props.getIsolationStrategy() == TenantIsolationStrategyEnum.LINE;
        }
    }

    protected static class OnParticipateByDefault implements Condition {
        @Override
        public boolean matches(ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {
            var props = HeliumConditions.bind(context.getEnvironment(), HeliumTenantProperties.class);
            return props.isParticipateByDefault();
        }
    }
}
