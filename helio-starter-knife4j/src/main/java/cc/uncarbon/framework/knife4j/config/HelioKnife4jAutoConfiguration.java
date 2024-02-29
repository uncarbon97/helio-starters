package cc.uncarbon.framework.knife4j.config;

import cc.uncarbon.framework.core.props.HelioProperties;
import cc.uncarbon.framework.knife4j.customizer.HelioBaseEnumCustomizer;
import cn.hutool.core.text.CharSequenceUtil;
import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.ConfigurableEnvironment;


/**
 * Helio knife4j 自动配置类
 *
 * @author Uncarbon
 * @author xiaoymin
 */
@Import(value = {HelioBaseEnumCustomizer.class})
@EnableKnife4j
@ConditionalOnExpression(value = "!${knife4j.production:false}")
@AutoConfiguration(before = SpringDocConfiguration.class)
public class HelioKnife4jAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(value = OpenAPI.class)
    public OpenAPI openApi(HelioProperties helioProperties, ConfigurableEnvironment env) {
        OpenAPI openApi = new OpenAPI();
        // 基本信息
        Info info = buildInfo(helioProperties);
        openApi.info(info);

        // 安全方案
        String headerTokenName = CharSequenceUtil.blankToDefault(env.getProperty("sa-token.token-name"), "Authorization");
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(headerTokenName);
        openApi.addSecurityItem(securityRequirement);

        return openApi;
    }

    private Info buildInfo(HelioProperties helioProperties) {
        Info info = new Info();
        HelioProperties.Knife4j knife4j = helioProperties.getKnife4j();
        info.setTitle(knife4j.getTitle());
        info.setDescription(knife4j.getDescription());
        info.setVersion(knife4j.getVersion());
        return info;
    }

}
