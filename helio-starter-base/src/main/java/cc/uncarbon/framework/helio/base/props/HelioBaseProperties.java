package cc.uncarbon.framework.helio.base.props;


import cc.uncarbon.framework.helio.base.constant.ConfigurationPropertiesPrefix;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Helio 基础配置属性类
 *
 * @author Uncarbon
 */
@ConfigurationProperties(prefix = ConfigurationPropertiesPrefix.BASE)
@Data
public class HelioBaseProperties {

    /**
     * 是否处于生产环境
     */
    private Boolean productionFlag;

}
