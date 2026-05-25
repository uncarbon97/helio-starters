package cc.uncarbon.framework.helium.tenant.datasource;

import cc.uncarbon.framework.helium.db.dynamicdatasource.helper.DynamicDataSourceHelper;
import cc.uncarbon.framework.helium.db.model.DataSourceSetting;
import cc.uncarbon.framework.helium.tenant.enums.TenantStrategyEnum;
import cc.uncarbon.framework.helium.tenant.props.HeliumTenantProperties;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.*;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;
import java.util.Optional;

/**
 * 数据源级多租户自动配置类
 * 基于 AOP 实现
 * <a href="https://blog.csdn.net/w57685321/article/details/106823660">参考文章</a>
 *
 * @author Uncarbon
 */
@Conditional(value = TenantDataSourceConfiguration.OnDatasourceStrategy.class)
@Slf4j
public class TenantDataSourceConfiguration {

    private static final String LOG_PREFIX = "[Framework][数据源级多租户]";


    @Bean
    @ConditionalOnMissingBean
    public TenantDataSourceSettingProvider defaultTenantDataSourceSettingProvider() {
        return tenantId -> Optional.empty();
    }

    @Bean
    @ConditionalOnMissingBean
    public TenantDataSourceAopInterceptor tenantDataSourceAopInterceptor(
            DynamicDataSourceHelper dynamicDataSourceHelper,
            TenantDataSourceSettingProvider tenantDataSourceSettingProvider
    ) {
        log.info(LOG_PREFIX + " 已挂载 AOP 拦截器");
        return new TenantDataSourceAopInterceptor(dynamicDataSourceHelper, tenantDataSourceSettingProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    public TenantDataSourceAopAdvisor tenantDataSourceAopAdvisor(TenantDataSourceAopInterceptor aopInterceptor,
                                                                 DynamicDataSourceProperties properties) {
        TenantDataSourceAopAdvisor advisor = new TenantDataSourceAopAdvisor(aopInterceptor);
        // 数值越高，优先度越低
        advisor.setOrder(properties.getAop().getOrder() + 1);
        log.info(LOG_PREFIX + " 已挂载 AOP 织入点");
        return advisor;
    }

    protected static final class OnDatasourceStrategy implements Condition {
        @Override
        public boolean matches(ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {
            var props = Objects.requireNonNull(context.getBeanFactory()).getBean(HeliumTenantProperties.class);
            return props.getStrategy() == TenantStrategyEnum.DATASOURCE;
        }
    }
}
