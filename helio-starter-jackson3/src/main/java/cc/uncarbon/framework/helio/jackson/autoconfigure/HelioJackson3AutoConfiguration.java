package cc.uncarbon.framework.helio.jackson.autoconfigure;


import cc.uncarbon.framework.helio.jackson.factory.JsonMapperFactory;
import cc.uncarbon.framework.helio.jackson.module.BaseEnumFormatModule;
import cc.uncarbon.framework.helio.jackson.module.BigintAsStringModule;
import cc.uncarbon.framework.helio.jackson.module.DateTimeFormatModule;
import cc.uncarbon.framework.helio.jackson.props.HelioJacksonProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.context.annotation.Bean;
import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.time.ZoneId;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Helio 集成 Jackson 3.x 自动配置类
 *
 * @author hanfeng
 * @author Uncarbon
 */
@EnableConfigurationProperties(value = {HelioJacksonProperties.class})
@AutoConfigureBefore({JacksonAutoConfiguration.class})
@AutoConfiguration
public class HelioJackson3AutoConfiguration {

    /**
     * 首选 {@link JsonMapper}
     */
    @Bean
    public JsonMapper jsonMapper(HelioJacksonProperties props) {
        return JsonMapperFactory.enhacedJsonMapper(
                Locale.getDefault(), TimeZone.getTimeZone(ZoneId.systemDefault()),
                true, props.getBaseEnum());
    }
}
