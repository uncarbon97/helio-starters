package cc.uncarbon.framework.helium.tenant.props;

import cc.uncarbon.framework.helium.base.constant.ConfigurationPropertiesPrefix;
import cc.uncarbon.framework.helium.tenant.constant.HeliumTenantConstant;
import cc.uncarbon.framework.helium.tenant.enums.TenantIsolationStrategyEnum;
import cc.uncarbon.framework.helium.tenant.enums.TenantLoginModeEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collection;
import java.util.Objects;

/**
 * Helium 多租户配置属性类
 *
 * @author Uncarbon
 */
@ConfigurationProperties(prefix = ConfigurationPropertiesPrefix.TENANT)
@Data
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
     * 默认租户ID
     * <p>语义仅用于「无租户上下文时的兜底写值」（如平台自营域），不用于登录回退</p>
     */
    private Long defaultTenantId;

    /**
     * 严格模式：上下文缺租户且 SQL 需要拼租户时抛出业务异常，而非静默拼接 NULL
     * <p>消灭子线程漏传上下文导致的「静默空结果」问题</p>
     */
    private Boolean strict;

    /**
     * 默认参与隔离：未显式忽略（ignored-tables / @TenantIgnore）的表一律参与租户隔离
     * <p>口径翻转开关，默认 false 保持兼容；开启前须确保参与表都有 tenant_id 列，
     * 建议配合启动期强对账（缺列启动失败）使用</p>
     */
    private Boolean participateByDefault;


    public TenantIsolationStrategyEnum getIsolationStrategy() {
        return Objects.requireNonNullElse(isolationStrategy, TenantIsolationStrategyEnum.NONE);
    }

    public TenantLoginModeEnum getLoginMode() {
        return Objects.requireNonNullElse(loginMode, TenantLoginModeEnum.TENANT_FIRST);
    }

    public Long getDefaultTenantId() {
        return Objects.requireNonNullElse(defaultTenantId, HeliumTenantConstant.FALLBACK_TENANT_ID);
    }

    public boolean getStrict() {
        return Boolean.TRUE.equals(strict);
    }

    public boolean isParticipateByDefault() {
        return Boolean.TRUE.equals(participateByDefault);
    }

    /**
     * 快速判断多租户特性是否启用
     */
    public boolean doesTenantEnabled() {
        return getIsolationStrategy() != TenantIsolationStrategyEnum.NONE;
    }

}
