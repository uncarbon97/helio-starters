package cc.uncarbon.framework.core.config;

import cn.hutool.core.util.StrUtil;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

/**
 * 兼容历史版本 自动配置类
 *
 * @author kong
 * @author Uncarbon
 */
@Configuration
@RequiredArgsConstructor
public class AdaptHistoryVersionAutoConfiguration implements EnvironmentAware {

    /**
     * 旧的配置keys
     */
    private static final String[] oldPropertyPrefixes = {"helio.crud.tenant."};

    /**
     * 新的配置keys
     */
    private static final String[] newPropertyPrefixes = {"helio.tenant."};

    /**
     * 过时属性警告提示文案
     */
    private final Set<String> deprecatedPropertyWarnings = new LinkedHashSet<>();


    @Override
    public void setEnvironment(@NonNull Environment env) {
        try {
            ConfigurableEnvironment c = (ConfigurableEnvironment) env;
            MutablePropertySources sources = c.getPropertySources();

            Map<String, Object> newMap = new LinkedHashMap<>();
            for (PropertySource<?> source : sources) {
                if (source instanceof OriginTrackedMapPropertySource) {
                    // 根据类判断是否为 SpringBoot .yml 或者 .properties 的配置
                    Map<String, Object> bootProp = ((OriginTrackedMapPropertySource) source).getSource();

                    for (String key : bootProp.keySet()) {
                        if (key != null) {
                            for (int i = 0; i < oldPropertyPrefixes.length; i++) {
                                if (key.startsWith(oldPropertyPrefixes[i])) {
                                    /*
                                    将配置文件中所有 [helio.crud.tenant.] 开头的配置转移到 [helio.tenant.] 下
                                    实践测试 只对 @Value 注解有效
                                     */
                                    String newKey = StrUtil.replace(key, oldPropertyPrefixes[i], newPropertyPrefixes[i]);
                                    newMap.put(newKey, bootProp.get(key));

                                    // 前缀相同的话，提示信息就保持一条（利用 Set 特性）
                                    deprecatedPropertyWarnings.add(
                                            StrUtil.format("配置文件属性前缀 {}* 已过时、不向下兼容，请转移到 {}* 下"
                                                    , oldPropertyPrefixes[i], newPropertyPrefixes[i])
                                    );
                                }
                            }
                        }
                    }
                }
            }

            // 追加到总配置里面
            if (newMap.size() > 0) {
                OriginTrackedMapPropertySource source = new OriginTrackedMapPropertySource(
                        "AdaptHistoryVersionAutoConfiguration", newMap);
                // 追加到末尾，优先级最低
                c.getPropertySources().addLast(source);

                System.err.println("\n"
                        + StrUtil.join("\n", deprecatedPropertyWarnings)
                        + "\n");
            }
        } catch (Exception e) {
            // ignored
        }
    }
}
