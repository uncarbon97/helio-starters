package cc.uncarbon.framework.helium.tenant.props;

import cc.uncarbon.framework.helium.base.constant.ConfigurationPropertiesPrefix;
import cc.uncarbon.framework.helium.tenant.enums.TenantLoginModeEnum;
import cc.uncarbon.framework.helium.tenant.enums.TenantStrategyEnum;
import cn.hutool.core.collection.CollUtil;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collection;

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
    private TenantStrategyEnum strategy;

    /**
     * 登录模式
     */
    private TenantLoginModeEnum loginMode;

    /**
     * 忽略拼接租户 ID 条件的表；仅用于行级多租户
     */
    private Collection<String> ignoredTables;

    public TenantStrategyEnum getStrategy() {
        if (strategy == null) {
            return TenantStrategyEnum.NONE;
        }
        return strategy;
    }

    public TenantLoginModeEnum getLoginMode() {
        if (loginMode == null) {
            return TenantLoginModeEnum.TENANT_FIRST;
        }
        return loginMode;
    }

    public boolean doesTenantEnabled() {
        return getStrategy() != TenantStrategyEnum.NONE;
    }

    public boolean canIgnoreTable(String tableName) {
        if (!doesTenantEnabled()) {
            return true;
        }
        return CollUtil.contains(ignoredTables, tableName);
    }
}
