package cc.uncarbon.framework.helium.i18n.annotation;

import cc.uncarbon.framework.helium.i18n.jackson.UtcInstantSerializer;
import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import tools.jackson.databind.annotation.JsonSerialize;

import java.lang.annotation.*;

/**
 * 标记 {@link java.time.Instant} 字段在序列化时跳过多时区换算，固定按 UTC（ISO-8601 {@code Z}）输出。
 *
 * <p>本质是 {@link JsonSerialize}{@code (using = UtcInstantSerializer.class)} 的语义化别名（借助
 * {@link JacksonAnnotationsInside} 让 Jackson 识别），仅用于 {@code Instant} 类型字段。
 *
 * @author Uncarbon
 */
@JsonSerialize(using = UtcInstantSerializer.class)
@JacksonAnnotationsInside
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface TimezoneIgnored {
}
