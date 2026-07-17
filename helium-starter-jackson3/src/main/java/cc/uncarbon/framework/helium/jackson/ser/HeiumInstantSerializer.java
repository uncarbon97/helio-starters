package cc.uncarbon.framework.helium.jackson.ser;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ext.javatime.ser.InstantSerializer;
import tools.jackson.databind.ext.javatime.ser.InstantSerializerBase;
import tools.jackson.databind.ext.javatime.ser.JSR310FormattedSerializerBase;
import tools.jackson.databind.ser.std.StdSerializer;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 把 {@link Instant} 序列化为 ISO-8601 字符串
 * 参考 {@link InstantSerializer}
 *
 * @author Uncarbon
 */
public class HeiumInstantSerializer extends InstantSerializerBase<Instant> {

    public static final HeiumInstantSerializer INSTANCE = new HeiumInstantSerializer();

    private static final DateTimeFormatter OFFSET_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @Override
    protected JSR310FormattedSerializerBase<?> withFormat(DateTimeFormatter dtf, Boolean useTimestamp, JsonFormat.Shape shape) {
        return null;
    }

    public HeiumInstantSerializer() {
        super(Instant.class, Instant::toEpochMilli, Instant::getEpochSecond, Instant::getNano,
                // null -> use 'value.toString()', default format
                null);
    }
}
