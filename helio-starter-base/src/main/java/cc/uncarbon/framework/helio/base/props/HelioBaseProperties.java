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

    /**
     * 数据存储时区，决定存储在 DB 中的时刻的时区
     * 例如："Asia/Shanghai"
     */
    private String dataStorageTimeZone;

    /**
     * 数据外显时区
     * 例如："Asia/Shanghai"
     */
    private String dataDisplayTimeZone;

}
