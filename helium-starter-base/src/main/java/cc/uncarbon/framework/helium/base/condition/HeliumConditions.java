package cc.uncarbon.framework.helium.base.condition;

import lombok.experimental.UtilityClass;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;

import java.util.Objects;

/**
 * 在 Condition 评估阶段安全地读取 {@link ConfigurationProperties} 绑定值。
 *
 * <p>不依赖 bean 是否已注册（条件评估时 PropertiesBean 可能尚未注册到 Spring 环境内
 * prefix 取自注解，字段走 getter
 *
 * @author Uncarbon
 */
@UtilityClass
public final class HeliumConditions {

    /**
     * 从 Environment 绑定出指定配置属性对象；未配置时返回反射创建的默认实例（字段为 null/默认值）。
     *
     * @param env       Spring 环境
     * @param propsType 标注了 {@link ConfigurationProperties} 的属性配置类
     * @param <T>       属性类型
     * @return 绑定结果；未配置则返回默认实例
     */
    public static <T> T bind(Environment env, Class<T> propsType) {
        // value()/prefix() 互为 @AliasFor，AnnotationUtils 已解析，读任一皆可
        String prefix = Objects.requireNonNull(AnnotationUtils.findAnnotation(propsType, ConfigurationProperties.class))
                .prefix();
        // 未配置时反射 new 一个默认实例，避免根对象 getter 链 NPE
        return Binder.get(env).bind(prefix, propsType)
                .orElseGet(() -> BeanUtils.instantiateClass(propsType));
    }
}
