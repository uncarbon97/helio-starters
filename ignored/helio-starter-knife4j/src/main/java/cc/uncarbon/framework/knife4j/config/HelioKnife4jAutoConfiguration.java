package cc.uncarbon.framework.knife4j.config;

import cc.uncarbon.framework.helio.core.props.HelioProperties;
import cc.uncarbon.framework.knife4j.customizer.HelioBaseEnumCustomizer;
import cc.uncarbon.framework.knife4j.filter.AbstractSecurityFilter;
import cc.uncarbon.framework.knife4j.filter.JakartaProductionSecurityFilter;
import cc.uncarbon.framework.knife4j.filter.JakartaServletSecurityBasicAuthFilter;
import cc.uncarbon.framework.knife4j.model.Knife4jHttpBasic;
import cc.uncarbon.framework.knife4j.model.Knife4jProperties;
import cn.hutool.core.text.CharSequenceUtil;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityScheme;
import jakarta.servlet.DispatcherType;
import lombok.NonNull;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.ConfigurableEnvironment;


/**
 * Helio knife4j 自动配置类
 * @since 2.4.0 只保留 knife4j-ui，并从 knife4j 源码中移植「HTTP Basic鉴权机制」和「生产环境下不再显示开发文档」2个功能
 *
 * @author Uncarbon
 * @author xiaoymin
 */
@Import(value = {HelioBaseEnumCustomizer.class, SpringDocConfiguration.class})
@EnableConfigurationProperties({Knife4jProperties.class, Knife4jHttpBasic.class})
@ConditionalOnExpression(value = "${knife4j.enable:false}")
@AutoConfiguration
public class HelioKnife4jAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(value = OpenAPI.class)
    public OpenAPI openApi(HelioProperties helioProperties, ConfigurableEnvironment env) {
        OpenAPI openApi = new OpenAPI();
        // 基本信息
        openApi.setInfo(buildInfo(helioProperties));

        // 全局安全要求
        String headerTokenName = CharSequenceUtil.blankToDefault(env.getProperty("sa-token.token-name"), "Authorization");
        openApi.schemaRequirement(headerTokenName, buildSecurityScheme());
        return openApi;
    }

    /**
     * Security with Basic Http
     * @param knife4jProperties Basic Properties
     * @return BasicAuthFilter
     */
    @Bean
    @ConditionalOnMissingBean(JakartaServletSecurityBasicAuthFilter.class)
    @ConditionalOnExpression("${knife4j.basic.enable:false}")
    public FilterRegistrationBean<JakartaServletSecurityBasicAuthFilter> securityBasicAuthFilter(@NonNull Knife4jProperties knife4jProperties) {
        JakartaServletSecurityBasicAuthFilter authFilter = new JakartaServletSecurityBasicAuthFilter();
        authFilter.setEnableBasicAuth(knife4jProperties.getBasic().isEnable());
        authFilter.setUserName(knife4jProperties.getBasic().getUsername());
        authFilter.setPassword(knife4jProperties.getBasic().getPassword());
        authFilter.addRule(knife4jProperties.getBasic().getInclude());

        FilterRegistrationBean<JakartaServletSecurityBasicAuthFilter> registration = new FilterRegistrationBean<>();
        registration.setDispatcherTypes(DispatcherType.REQUEST);
        registration.setFilter(authFilter);
        registration.setOrder(AbstractSecurityFilter.SPRING_FILTER_ORDER);
        return registration;
    }

    @Bean
    @ConditionalOnMissingBean(JakartaProductionSecurityFilter.class)
    @ConditionalOnExpression("${knife4j.production:false}")
    public FilterRegistrationBean<JakartaProductionSecurityFilter> productionSecurityFilter(@NonNull Knife4jProperties knife4jProperties) {
        JakartaProductionSecurityFilter prodFilter = new JakartaProductionSecurityFilter(knife4jProperties.isProduction());

        FilterRegistrationBean<JakartaProductionSecurityFilter> registration = new FilterRegistrationBean<>();
        registration.setDispatcherTypes(DispatcherType.REQUEST);
        registration.setFilter(prodFilter);
        registration.setOrder(AbstractSecurityFilter.SPRING_FILTER_ORDER - 1);
        return registration;
    }

    private Info buildInfo(HelioProperties helioProperties) {
        Info info = new Info();
        HelioProperties.Knife4j knife4j = helioProperties.getKnife4j();
        info.setTitle(knife4j.getTitle());
        info.setDescription(knife4j.getDescription());
        info.setVersion(knife4j.getVersion());
        return info;
    }

    /**
     * 利用OAuth2的安全规则，骗出 knife4j-ui 的请求头部输入框
     */
    private SecurityScheme buildSecurityScheme() {
        OAuthFlow clientCredential = new OAuthFlow();
        clientCredential.setTokenUrl("本页面无作用，只要请求接口时，在请求头带上Token即可");
        OAuthFlows oauthFlows = new OAuthFlows();
        oauthFlows.password(clientCredential);
        SecurityScheme securityScheme = new SecurityScheme();
        securityScheme.setType(SecurityScheme.Type.OAUTH2);
        securityScheme.setFlows(oauthFlows);
        return securityScheme;
    }

}
