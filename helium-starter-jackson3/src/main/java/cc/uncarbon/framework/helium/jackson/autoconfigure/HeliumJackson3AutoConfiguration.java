package cc.uncarbon.framework.helium.jackson.autoconfigure;


import cc.uncarbon.framework.helium.jackson.factory.JsonMapperFactory;
import cc.uncarbon.framework.helium.jackson.module.BaseEnumFormatModule;
import cc.uncarbon.framework.helium.jackson.module.BigintAsStringModule;
import cc.uncarbon.framework.helium.jackson.module.DateTimeFormatModule;
import cc.uncarbon.framework.helium.jackson.props.HeliumJacksonProperties;
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
 * Helium 集成 Jackson 3.x 自动装配类
 *
 * @author hanfeng
 * @author Uncarbon
 */
@EnableConfigurationProperties(value = {HeliumJacksonProperties.class})
@AutoConfigureBefore({JacksonAutoConfiguration.class})
@AutoConfiguration
public class HeliumJackson3AutoConfiguration {

    /**
     * 首选 {@link JsonMapper}
     */
    @Bean
    public JsonMapper jsonMapper(HeliumJacksonProperties props) {
        return JsonMapperFactory.enhacedJsonMapper(
                Locale.getDefault(), TimeZone.getTimeZone(ZoneId.systemDefault()),
                true, props.getBaseEnum());
    }
}
