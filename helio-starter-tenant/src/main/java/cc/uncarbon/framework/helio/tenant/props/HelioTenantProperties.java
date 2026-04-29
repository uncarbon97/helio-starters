package cc.uncarbon.framework.helio.tenant.props;

import cc.uncarbon.framework.helio.base.constant.ConfigurationPropertiesPrefix;
import cc.uncarbon.framework.helio.tenant.enums.TenantStrategyEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collection;

/**
 * Helio 多租户配置属性类
 *
 * @author Uncarbon
 */
@ConfigurationProperties(prefix = ConfigurationPropertiesPrefix.TENANT)
@Data
public class HelioTenantProperties {

    /**
     * 多租户策略
     */
    private TenantStrategyEnum strategy;

    /**
     * 忽略拼接租户 ID 条件的表；仅用于行级多租户
     */
    private Collection<String> ignoredTables;

}
