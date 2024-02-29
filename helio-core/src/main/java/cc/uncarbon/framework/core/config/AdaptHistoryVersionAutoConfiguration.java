package cc.uncarbon.framework.core.config;

import cn.hutool.core.text.CharSequenceUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * 兼容历史版本 自动配置类
 *
 * @author Uncarbon
 */
@RequiredArgsConstructor
@AutoConfiguration
@Slf4j
public class AdaptHistoryVersionAutoConfiguration implements EnvironmentAware {

    /**
     * 旧的配置keys
     */
    private static final String[] OLD_PROPERTY_PREFIXES = {"helio.crud.tenant."};

    /**
     * 新的配置keys
     */
    private static final String[] NEW_PROPERTY_PREFIXES = {"helio.tenant."};

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
                // 根据类判断是否为 SpringBoot .yml 或者 .properties 的配置
                if (source instanceof OriginTrackedMapPropertySource originTrackedMapPropertySource) {
                    processPropertySource(originTrackedMapPropertySource, newMap);
                }
            }

            if (!newMap.isEmpty()) {
                // 追加到总配置里面
                OriginTrackedMapPropertySource source = new OriginTrackedMapPropertySource(
                        "AdaptHistoryVersionAutoConfiguration", newMap);
                // 追加到末尾，优先级最低
                c.getPropertySources().addLast(source);

                log.warn("\n{}\n", CharSequenceUtil.join("\n", deprecatedPropertyWarnings));
            }
        } catch (Exception e) {
            // ignored
        }
    }

    /**
     * 将配置文件中所有 [helio.crud.tenant.] 开头的配置转移到 [helio.tenant.] 下
     * 实践测试 只对 @Value 注解有效
     */
    private void processPropertySource(OriginTrackedMapPropertySource source, Map<String, Object> newMap) {
        Map<String, Object> bootProp = source.getSource();

        for (Map.Entry<String, Object> entry : bootProp.entrySet()) {
            String key = entry.getKey();
            for (int i = 0; i < OLD_PROPERTY_PREFIXES.length; i++) {
                if (key.startsWith(OLD_PROPERTY_PREFIXES[i])) {
                    String newKey = key.replace(OLD_PROPERTY_PREFIXES[i], NEW_PROPERTY_PREFIXES[i]);
                    newMap.put(newKey, bootProp.get(key));

                    // 前缀相同的话，提示信息就保持一条（利用 Set 特性）
                    deprecatedPropertyWarnings.add(
                            CharSequenceUtil.format("配置文件属性前缀 {}* 已过时、不向下兼容，请转移到 {}* 下",
                                    OLD_PROPERTY_PREFIXES[i], NEW_PROPERTY_PREFIXES[i])
                    );
                }
            }
        }
    }
}
