package cc.uncarbon.framework.helium.websecurity.props;

import cc.uncarbon.framework.helium.base.constant.ConfigurationPropertiesPrefix;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

import java.util.List;

/**
 * Helium Web 网络安全配置属性类
 *
 * @author Uncarbon
 */
@Data
@ConfigurationProperties(prefix = ConfigurationPropertiesPrefix.WEB_SECURITY)
public class HeliumWebSecurityProperties {

    private AntiXss antiXss = new AntiXss();

    private Cors cors = new Cors();

    @Data
    public static final class AntiXss {

        /**
         * 是否启用
         */
        private Boolean enabled = true;

        /**
         * 不进行过滤的路由（直接放行）
         */
        private List<String> ignoredPaths;

        /**
         * JSON 请求体缓冲上限；XSS 过滤需整段读入内存，超过即响应 413。默认 2MB，设为 0 或负数表示不限制（不建议）
         */
        private DataSize maxBodySize = DataSize.ofMegabytes(2);

    }

    @Data
    public static final class Cors {

        /**
         * 允许跨域的来源列表（支持通配模式，如 https://*.example.com）；
         * 出于安全考虑不再默认放行任意来源，默认仅放行本机开发前端
         */
        private List<String> allowedOrigins = List.of("http://localhost:*", "http://127.0.0.1:*");

    }

}
