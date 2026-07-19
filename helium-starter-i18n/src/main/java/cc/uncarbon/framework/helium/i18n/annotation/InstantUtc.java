package cc.uncarbon.framework.helium.i18n.annotation;

import cc.uncarbon.framework.helium.i18n.jackson.InstantUtcSerializer;
import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import tools.jackson.databind.annotation.JsonSerialize;

import java.lang.annotation.*;
import java.time.Instant;

/**
 * 标记 {@link Instant} 类型字段在序列化时跳过多时区换算，固定按 UTC 时区输出
 *
 * @author Uncarbon
 */
@JsonSerialize(using = InstantUtcSerializer.class)
@JacksonAnnotationsInside
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface InstantUtc {
}
