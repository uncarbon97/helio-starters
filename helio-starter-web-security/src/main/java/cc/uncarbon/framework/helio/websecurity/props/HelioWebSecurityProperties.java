package cc.uncarbon.framework.helio.websecurity.props;

import cc.uncarbon.framework.helio.base.constant.ConfigurationPropertiesPrefix;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Helio Web 网络安全配置属性类
 *
 * @author Uncarbon
 */
@ConfigurationProperties(prefix = ConfigurationPropertiesPrefix.WEB_SECURITY)
@Data
public class HelioWebSecurityProperties {

    private AntiXss antiXss;

    @Data
    public static final class AntiXss {

        /**
         * 是否启用
         */
        private Boolean enabled;

        /**
         * 不进行过滤的路由（直接放行）
         */
        private List<String> ignoredUrls;

    }

}
