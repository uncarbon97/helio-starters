package cc.uncarbon.framework.helio.jackson.factory;

import cc.uncarbon.framework.helio.jackson.module.BaseEnumFormatModule;
import cc.uncarbon.framework.helio.jackson.module.BigintAsStringModule;
import cc.uncarbon.framework.helio.jackson.module.DateTimeFormatModule;
import cc.uncarbon.framework.helio.jackson.props.BaseEnumConfig;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.experimental.UtilityClass;
import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.util.Locale;
import java.util.TimeZone;

@UtilityClass
public class JsonMapperFactory {

    /**
     * 创建一个增强过的 {@link JsonMapper} 实例
     *
     * @param useBigintAsString 使用【大整数转字符串】
     * @param baseEnumConfig    主要调整 {@link BaseEnumFormatModule} 的行为
     */
    public JsonMapper enhacedJsonMapper(Locale defaultLocale, TimeZone defaultTimeZone,
                                        boolean useBigintAsString, BaseEnumConfig baseEnumConfig) {
        JsonMapper.Builder builder = JsonMapper.builder();
        builder.defaultLocale(defaultLocale)
                .defaultTimeZone(defaultTimeZone)
                .enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS)
                .enable(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER)
                .enable(JsonReadFeature.ALLOW_SINGLE_QUOTES)
                .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
                .addModule(new DateTimeFormatModule())
                .addModule(new BaseEnumFormatModule(baseEnumConfig));

        if (useBigintAsString) {
            builder.addModule(new BigintAsStringModule());
        }
        builder.changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.ALWAYS))
                .findAndAddModules();
        return builder.build();
    }
}
