package cc.uncarbon.framework.helio.tenant.props;

import cc.uncarbon.framework.helio.tenant.enums.TenantStrategyEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "helio.tenant")
@Data
public class HelioTenantProperties {

    /**
     * 多租户策略
     */
    private TenantStrategyEnum strategy;

}
