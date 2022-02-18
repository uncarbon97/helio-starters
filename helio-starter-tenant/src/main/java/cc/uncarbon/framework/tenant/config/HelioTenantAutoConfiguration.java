package cc.uncarbon.framework.tenant.config;

import cc.uncarbon.framework.core.context.TenantContextHolder;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HelioTenantAutoConfiguration {

    public HelioTenantAutoConfiguration() {
        TenantContextHolder.setEnableTenant(true);
    }
}
