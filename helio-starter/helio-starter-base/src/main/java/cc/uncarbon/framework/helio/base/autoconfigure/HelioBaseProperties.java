package cc.uncarbon.framework.helio.base.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "helio.base")
@Data
public class HelioBaseProperties {

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
