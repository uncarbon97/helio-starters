package cc.uncarbon.framework.helium.tenant.line;

import cc.uncarbon.framework.helium.base.condition.HeliumConditions;
import cc.uncarbon.framework.helium.tenant.enums.TenantStrategyEnum;
import cc.uncarbon.framework.helium.tenant.props.HeliumTenantProperties;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.*;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Collection;
import java.util.Objects;

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
        Collection<String> ignoredTables = props.getIgnoredTables();
        log.info(LOG_PREFIX + " 已应用 >> 显式忽略的数据表: {}；其余数据表根据实体是否实现 TenantEntity 接口自动判断", ignoredTables);
        return new TenantLineInnerInterceptor(new DefaultTenantLineHandler(ignoredTables));
    }

    protected static class OnLineStrategy implements Condition {
        @Override
        public boolean matches(ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {
            var props = HeliumConditions.bind(context.getEnvironment(), HeliumTenantProperties.class);
            return props.getStrategy() == TenantStrategyEnum.LINE;
        }
    }
}
