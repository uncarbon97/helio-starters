package cc.uncarbon.framework.helium.websecurity.props;

import cc.uncarbon.framework.helium.base.constant.ConfigurationPropertiesPrefix;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Helium Web 网络安全配置属性类
 *
 * @author Uncarbon
 */
@ConfigurationProperties(prefix = ConfigurationPropertiesPrefix.WEB_SECURITY)
@Data
public class HeliumWebSecurityProperties {

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
        private List<String> ignoredPaths;

    }

}
