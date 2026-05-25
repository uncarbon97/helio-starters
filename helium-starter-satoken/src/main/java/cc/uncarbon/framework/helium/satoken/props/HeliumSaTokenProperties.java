package cc.uncarbon.framework.helium.satoken.props;

import cc.uncarbon.framework.helium.base.constant.ConfigurationPropertiesPrefix;
import cc.uncarbon.framework.helium.satoken.dao.SaTokenRedisDaoWithLocalCache;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Helium 集成 SA-Token 配置属性类
 *
 * @author Uncarbon
 */
@ConfigurationProperties(prefix = ConfigurationPropertiesPrefix.SA_TOKEN)
@Data
public class HeliumSaTokenProperties {

    /**
     * @see SaTokenRedisDaoWithLocalCache
     */
    private RedisDaoWithLocalCache redisDaoWithLocalCache;

    @Data
    public static final class RedisDaoWithLocalCache {

        /**
         * 是否启用
         */
        private Boolean enabled = false;

        /**
         * 本地缓存有效时长，单位=秒
         */
        private long duration = 5;

        /**
         * 本地缓存最大容量，注意：每次超过容量都会触发一次清理过程
         */
        private int capacity = 4000;

    }

}
