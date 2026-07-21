package cc.uncarbon.framework.helium.web.support;

import cc.uncarbon.framework.helium.web.jackson.EnumConverterFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.*;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebBindingInitializer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;

/**
 * 增强 WebMVC 默认行为
 *
 * @author Uncarbon
 */
@RequiredArgsConstructor
@Slf4j
public class EnhanceWebMvcConfigurer implements WebMvcConfigurer, WebBindingInitializer {

    private final JsonMapper jsonMapper;


    /**
     * 对于 GET 请求，将所有参数的空格 trim
     */
    @Override
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.registerCustomEditor(String.class, new StringTrimmerEditor(false));
    }

    /**
     * 对于 GET 请求，将 int 值转换成枚举
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverterFactory(new EnumConverterFactory());
    }

    /**
     * 使用 Jackson 作为 JSON MessageConverter
     * 消息转换，内置断点续传，下载和字符串
     */
    @Override
    public void configureMessageConverters(HttpMessageConverters.@NonNull ServerBuilder builder) {
        builder.addCustomConverter(new StringHttpMessageConverter(StandardCharsets.UTF_8));
        builder.addCustomConverter(new ByteArrayHttpMessageConverter());
        builder.addCustomConverter(new ResourceHttpMessageConverter());
        builder.addCustomConverter(new ResourceRegionHttpMessageConverter());
        builder.addCustomConverter(new JacksonJsonHttpMessageConverter(jsonMapper));
    }
}
