package cc.uncarbon.framework.helium.tenant.props;

import cc.uncarbon.framework.helium.base.constant.ConfigurationPropertiesPrefix;
import cc.uncarbon.framework.helium.tenant.enums.TenantStrategyEnum;
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
     * 忽略拼接租户 ID 条件的表；仅用于行级多租户
     */
    private Collection<String> ignoredTables;

}
