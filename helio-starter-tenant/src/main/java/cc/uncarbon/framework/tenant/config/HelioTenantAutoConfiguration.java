package cc.uncarbon.framework.tenant.config;

import cc.uncarbon.framework.core.props.HelioProperties;
import cc.uncarbon.framework.tenant.handler.HelioTenantLineHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "helio.tenant", value = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class HelioTenantAutoConfiguration {

    private final HelioProperties helioProperties;


    @Bean
    public HelioTenantLineHandler helioTenantLineHandler() {
        return new HelioTenantLineHandler(helioProperties.getTenant().getIgnoredTables());
    }

}
