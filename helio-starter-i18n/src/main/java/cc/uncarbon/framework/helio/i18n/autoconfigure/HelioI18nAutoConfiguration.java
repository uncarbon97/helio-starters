package cc.uncarbon.framework.helio.i18n.autoconfigure;

import cc.uncarbon.framework.helio.i18n.message.YamlMessageSource;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;


/**
 * Helio 集成国际化自动配置类
 *
 * @author Uncarbon
 */
@RequiredArgsConstructor
@AutoConfiguration
public class HelioI18nAutoConfiguration {

    @Bean
    public MessageSource yamlMessageSource() {
        return new YamlMessageSource();
    }

}