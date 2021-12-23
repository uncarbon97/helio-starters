package cc.uncarbon.framework.web.config;

import cc.uncarbon.framework.web.jackson.EnumConverterFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebBindingInitializer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 默认WebMVC配置
 *
 * @author Uncarbon
 **/
@Slf4j
@Configuration
public class DefaultWebMvcAutoConfiguration implements WebMvcConfigurer, WebBindingInitializer {
    /**
     * 自定义静态资源
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
        registry
                .addResourceHandler("/doc.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry
                .addResourceHandler("/swagger-resources")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry
                .addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    /**
     * [GET]请求, 将所有参数的空格trim
     */
    @Override
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.registerCustomEditor(String.class, new StringTrimmerEditor(false));
    }

    /**
     * [GET]请求, 将int值转换成枚举类
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverterFactory(new EnumConverterFactory());
    }
}
