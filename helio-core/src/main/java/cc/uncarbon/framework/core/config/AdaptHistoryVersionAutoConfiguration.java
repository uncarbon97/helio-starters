package cc.uncarbon.framework.core.config;

import cn.hutool.core.util.StrUtil;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import lombok.NonNull;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
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
@AutoConfigureAfter(HelioPropertiesAutoConfiguration.class)
@Configuration
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
    private final Set<String> deprecatePropertyWarnings = new LinkedHashSet<>();


    @Override
    public void setEnvironment(@NonNull Environment env) {
        try {
            ConfigurableEnvironment c = (ConfigurableEnvironment) env;
            MutablePropertySources sources = c.getPropertySources();

            Map<String, Object> newMap = new LinkedHashMap<>();
            for (PropertySource<?> source : sources) {
                // 根据 Name 开头单词判断是否为 SpringBoot .yml 或者 .properties 的配置
                if (source.getName().startsWith("applicationConfig")) {
                    Map<String, Object> bootProp = (Map<String, Object>) source.getSource();
                    for (String key : bootProp.keySet()) {
                        if (key != null) {
                            for (int i = 0; i < oldPropertyPrefixes.length; i++) {
                                if (key.startsWith(oldPropertyPrefixes[i])) {
                                    // e.g. 将配置文件中所有 [helio.crud.tenant.] 开头的配置转移到 [helio.tenant.] 下
                                    String newKey = StrUtil.replace(key, oldPropertyPrefixes[i], newPropertyPrefixes[i]);
                                    newMap.put(newKey, bootProp.get(key));
                                    deprecatePropertyWarnings.add(
                                            StrUtil.format("原配置文件属性前缀 {} 已兼容转移到 {} 下")
                                    );
                                }
                            }
                        }
                    }
                }
            }

            // 追加到总配置里面
            if (newMap.size() > 0) {
                System.err.println("\n"
                        + StrUtil.join("\n", deprecatePropertyWarnings)
                        + "当前版本暂时向下兼容，未来版本可能会完全移除旧形式");
                OriginTrackedMapPropertySource source = new OriginTrackedMapPropertySource(
                        "SaHistoryVersionInjectProperty", newMap);
                // 追加到末尾，优先级最低
                c.getPropertySources().addLast(source);
            }
        } catch (Exception e) {
            // ignored
        }
    }
}
