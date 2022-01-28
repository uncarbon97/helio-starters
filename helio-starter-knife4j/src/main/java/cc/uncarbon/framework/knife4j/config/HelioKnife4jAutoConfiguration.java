package cc.uncarbon.framework.knife4j.config;


import cc.uncarbon.framework.core.props.HelioProperties;
import cn.hutool.core.collection.CollUtil;
import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;


/**
 * Helio knife4j 自动配置类
 * 参考http://events.jianshu.io/p/2f19c1863da0
 *
 * @author Uncarbon
 * @author xiaoymin
 */
@EnableOpenApi
@EnableKnife4j
@Import(BeanValidatorPluginsConfiguration.class)
@Configuration
public class HelioKnife4jAutoConfiguration {

    @Value(value = "${sa-token.token-name:Authorization}")
    private String HEADER_TOKEN_NAME;

    @Resource
    private HelioProperties helioProperties;


    @Bean
    @Order(value = 1)
    public Docket groupRestApi() {
        return new Docket(DocumentationType.OAS_30)
                .apiInfo(this.apiInfo())
                .select()
                // 对所有API进行抓取
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build()
                // 在调试页上附加请求头
                .securityContexts(CollUtil.newArrayList(this.securityContext()))
                .securitySchemes(CollUtil.newArrayList(this.apiKeyOfToken()))
                ;
    }

    private ApiInfo apiInfo(){
        return new ApiInfoBuilder()
                .title(helioProperties.getKnife4j().getTitle())
                .description(helioProperties.getKnife4j().getDescription())
                .termsOfServiceUrl("")
                .contact(ApiInfo.DEFAULT_CONTACT)
                .version(helioProperties.getKnife4j().getVersion())
                .build();
    }

    private SecurityContext securityContext() {
        List<SecurityReference> references = new ArrayList<>();

        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;

        references.add(new SecurityReference(HEADER_TOKEN_NAME, authorizationScopes));

        return SecurityContext.builder()
                .securityReferences(references)
                .build();
    }

    private ApiKey apiKeyOfToken() {
        return new ApiKey(HEADER_TOKEN_NAME, HEADER_TOKEN_NAME, "header");
    }

}
