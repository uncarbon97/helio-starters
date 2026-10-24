package cc.uncarbon.framework.helium.tenant.props;

import cc.uncarbon.framework.helium.base.constant.ConfigurationPropertiesPrefix;
import cc.uncarbon.framework.helium.tenant.enums.TenantIsolationStrategyEnum;
import cc.uncarbon.framework.helium.tenant.enums.TenantLoginModeEnum;
import cc.uncarbon.framework.helium.tenant.line.TenantLineFieldReconciler;
import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collection;
import java.util.Objects;

/**
 * Helium 多租户配置属性类
 *
 * @author Uncarbon
 */
@Data
@ConfigurationProperties(prefix = ConfigurationPropertiesPrefix.TENANT)
public class HeliumTenantProperties {

    /**
     * 多租户策略
     */
    private TenantIsolationStrategyEnum isolationStrategy;

    /**
     * 多租户登录模式
     */
    private TenantLoginModeEnum loginMode;

    /**
     * 忽略拼接租户 ID 条件的表；仅用于行级多租户
     */
    private Collection<String> ignoredTables;

    /**
     * 严格模式：上下文缺租户且 SQL 需要拼租户时抛出业务异常，而非静默拼接 NULL
     * <p>消灭缺少租户上下文导致的「静默空结果」问题；默认开启
     */
    private Boolean strict = Boolean.TRUE;

    /**
     * 非严格模式：上下文缺租户时的兜底租户 ID；不用于登录回退
     */
    @Positive
    private Long nonStrictFallbackTenantId;

    /**
     * 默认参与隔离：未显式忽略（ignored-tables / @TenantIgnore）的表一律参与租户隔离
     * <p>默认 false 保持兼容；开启前须确保参与表都有 tenant_id 列
     * 配合 {@link TenantLineFieldReconciler} 使用</p>
     */
    private Boolean participateByDefault;

    /**
     * 回落即拒绝：与 strict 解耦的独立开关；开启后，上下文缺租户或租户数据源切换失败时
     * 直接抛异常拒绝执行，而非回落 primary 共享库（仅数据源级隔离生效）
     */
    private Boolean rejectOnFallback;


    /**
     * 启动期校验：兜底租户 ID 配成 0/负数时立即失败，避免生成 tenant_id = 0 的静默空结果
     */
    @PostConstruct
    void init() {
        if (nonStrictFallbackTenantId != null && nonStrictFallbackTenantId <= 0) {
            throw new IllegalStateException("配置项 " + ConfigurationPropertiesPrefix.TENANT
                    + ".non-strict-fallback-tenant-id 须为正整数，当前: " + nonStrictFallbackTenantId);
        }
    }

    public TenantIsolationStrategyEnum getIsolationStrategy() {
        return Objects.requireNonNullElse(isolationStrategy, TenantIsolationStrategyEnum.NONE);
    }

    public TenantLoginModeEnum getLoginMode() {
        return Objects.requireNonNullElse(loginMode, TenantLoginModeEnum.TENANT_FIRST);
    }

    public boolean isStrict() {
        // 默认严格模式：仅显式配置 false 才退回旧行为
        return !Boolean.FALSE.equals(strict);
    }

    public boolean isParticipateByDefault() {
        return Boolean.TRUE.equals(participateByDefault);
    }

    public boolean isRejectOnFallback() {
        return Boolean.TRUE.equals(rejectOnFallback);
    }

    /**
     * 快速判断多租户特性是否启用
     */
    public boolean doesTenantEnabled() {
        return getIsolationStrategy() != TenantIsolationStrategyEnum.NONE;
    }

}
