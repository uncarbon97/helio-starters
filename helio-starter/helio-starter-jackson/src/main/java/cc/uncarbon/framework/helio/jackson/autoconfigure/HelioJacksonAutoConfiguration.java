package cc.uncarbon.framework.helio.jackson.autoconfigure;


import cc.uncarbon.framework.helio.jackson.module.BaseEnumFormatModuleBak;
import cc.uncarbon.framework.helio.jackson.module.BigintAsStringModule;
import cc.uncarbon.framework.helio.jackson.module.DateTimeFormatModule;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.databind.json.JsonMapper;

import java.time.ZoneId;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Helio 集成Jackson自动配置类
 *
 * @author Zhu JW
 * @author Uncarbon
 */
//@AutoConfigureBefore({JacksonAutoConfiguration.class})
@AutoConfiguration
public class HelioJacksonAutoConfiguration {

    @Bean
    public JsonMapper jsonMapper() {
        Locale locale = Locale.getDefault();

        return JsonMapper.builder()
                .defaultLocale(locale)
                .defaultTimeZone(TimeZone.getTimeZone(ZoneId.systemDefault()))
                .enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS)
                .enable(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER)
                .enable(JsonReadFeature.ALLOW_SINGLE_QUOTES)
                .enable(tools.jackson.databind.SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
                .addModule(new BigintAsStringModule())
                .addModule(new DateTimeFormatModule())
                .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.ALWAYS))
                .findAndAddModules()
                .build();
    }
}
