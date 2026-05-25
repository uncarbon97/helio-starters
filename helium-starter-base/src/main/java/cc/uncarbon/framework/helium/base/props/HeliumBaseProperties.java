package cc.uncarbon.framework.helium.base.props;


import cc.uncarbon.framework.helium.base.constant.ConfigurationPropertiesPrefix;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Helium 基础配置属性类
 *
 * @author Uncarbon
 */
@ConfigurationProperties(prefix = ConfigurationPropertiesPrefix.BASE)
@Data
public class HeliumBaseProperties {

    /**
     * 是否处于生产环境
     */
    private Boolean productionFlag;

}
