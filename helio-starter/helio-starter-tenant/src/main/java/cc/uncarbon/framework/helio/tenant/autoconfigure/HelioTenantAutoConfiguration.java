package cc.uncarbon.framework.helio.tenant.autoconfigure;

import cc.uncarbon.framework.helio.base.enums.TenantIsolateLevelEnum;
import cc.uncarbon.framework.helio.base.autoconfigure.HelioProperties;
import cc.uncarbon.framework.crud.support.TenantSupport;
import cc.uncarbon.framework.crud.support.impl.DefaultTenantSupport;
import cc.uncarbon.framework.helio.tenant.enums.TenantStrategyEnum;
import cc.uncarbon.framework.helio.tenant.line.DefaultTenantLineHandler;
import cc.uncarbon.framework.helio.tenant.props.HelioTenantProperties;
import cc.uncarbon.framework.tenant.support.TenantDataSourceSupport;
import cc.uncarbon.framework.tenant.support.TenantLineSupport;
import ch.qos.logback.classic.Logger;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingFilterBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Collection;
import java.util.Objects;

/**
 * Helio 多租户自动配置类
 *
 * @author Uncarbon
 */
@EnableConfigurationProperties(value = {HelioTenantProperties.class})
@AutoConfiguration
@Slf4j
public class HelioTenantAutoConfiguration {

    private static final String LOG_PREFIX = "[Framework][多租户]";

    /**
     * 基于 mybatis-plus 的行级租户拦截器
     */
    @Conditional(value = OnLineStrategy.class)
    @Bean
    public TenantLineInnerInterceptor tenantLineInnerInterceptor(HelioTenantProperties props) {
        Collection<String> ignoredTables = props.getIgnoredTables();
        log.info(LOG_PREFIX + " 已应用行级租户 >> 以下数据表不参与租户隔离: {}", ignoredTables);
        return new TenantLineInnerInterceptor(new DefaultTenantLineHandler(ignoredTables));
    }

    private static class OnLineStrategy implements Condition {
        @Override
        public boolean matches(ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {
            var props = Objects.requireNonNull(context.getBeanFactory()).getBean(HelioTenantProperties.class);
            return props.getStrategy() == TenantStrategyEnum.LINE;
        }
    }


    private static class OnDatasourceStrategy implements Condition {
        @Override
        public boolean matches(ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {
            var props = Objects.requireNonNull(context.getBeanFactory()).getBean(HelioTenantProperties.class);
            return props.getStrategy() == TenantStrategyEnum.DATASOURCE;
        }
    }
//
//    @Bean
//    @Primary
//    public TenantSupport tenantSupport() {
//        if (!Boolean.TRUE.equals(helioProperties.getTenant().getEnabled())) {
//            // 引入了 starter，但未启用多租户
//            return new DefaultTenantSupport();
//        }
//
//        TenantIsolateLevelEnum isolateLevel = helioProperties.getTenant().getIsolateLevel();
//        if (isolateLevel == TenantIsolateLevelEnum.LINE) {
//            // 行级
//            return new TenantLineSupport();
//        } else if (isolateLevel == TenantIsolateLevelEnum.DATASOURCE) {
//            // 数据源级
//            return new TenantDataSourceSupport();
//        }
//        throw new IllegalArgumentException("启用多租户功能后，请正确配置对应的多租户隔离级别(helio.tenant.isolate-level)");
//    }
}
