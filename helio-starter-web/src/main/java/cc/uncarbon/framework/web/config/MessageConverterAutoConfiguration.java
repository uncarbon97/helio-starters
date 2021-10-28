package cc.uncarbon.framework.web.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.*;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Jackson类型转换器配置
 *
 * @author Zhu JW
 **/
@AllArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
@Configuration
public class MessageConverterAutoConfiguration implements WebMvcConfigurer {

    private final ObjectMapper objectMapper;


    /**
     * 使用 Jackson 作为JSON MessageConverter
     * 消息转换，内置断点续传，下载和字符串
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.removeIf(x -> x instanceof StringHttpMessageConverter || x instanceof AbstractJackson2HttpMessageConverter);
        converters.add(new StringHttpMessageConverter(StandardCharsets.UTF_8));
        converters.add(new ByteArrayHttpMessageConverter());
        converters.add(new ResourceHttpMessageConverter());
        converters.add(new ResourceRegionHttpMessageConverter());
        converters.add(new MappingJackson2HttpMessageConverter(objectMapper));
    }
}
