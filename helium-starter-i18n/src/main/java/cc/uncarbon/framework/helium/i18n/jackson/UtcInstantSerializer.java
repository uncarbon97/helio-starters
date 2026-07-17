package cc.uncarbon.framework.helium.i18n.jackson;

import cc.uncarbon.framework.helium.i18n.annotation.TimezoneIgnored;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

import java.time.Instant;

/**
 * 把 {@link Instant} 固定按 UTC 输出（ISO-8601，形如 {@code 2026-01-01T04:34:56Z}），
 * 不做客户端时区换算。
 *
 * <p>配合 {@link TimezoneIgnored} 使用：被标记的绝对时刻字段跳过多时区换算。
 *
 * @author Uncarbon
 */
public class UtcInstantSerializer extends StdSerializer<Instant> {

    public static final UtcInstantSerializer INSTANCE = new UtcInstantSerializer();

    public UtcInstantSerializer() {
        super(Instant.class);
    }

    @Override
    public void serialize(Instant value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
        // Instant.toString() 形如 2026-01-01T04:34:56Z
        gen.writeString(value.toString());
    }
}
