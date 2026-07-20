package cc.uncarbon.framework.helium.jackson.module;

import cc.uncarbon.framework.helium.jackson.constant.Jackson3Constant;
import cn.hutool.core.date.DatePattern;
import org.jspecify.annotations.NonNull;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.core.Version;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.StdSerializer;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * {@link Instant} 的序列化/反序列化格式模块
 * <P> 序列化格式: {@link DatePattern#UTC_WITH_XXX_OFFSET_PATTERN}（系统默认时区 offset，无毫秒）
 * <P> 反序列化: 按同一格式解析，回退兼容 ISO-8601 {@code ...Z}
 * <P> 替代 Jackson 内建的 {@link tools.jackson.databind.ext.javatime.ser.InstantSerializer}
 *
 * @author Uncarbon
 **/
public class InstantNoMillisFormatModule extends SimpleModule {

    public static final ZoneId OS_ZONE = ZoneId.systemDefault();

    public DateTimeFormatter formatter = Jackson3Constant.UTC_WITH_XXX_OFFSET_FORMATTER;

    public InstantNoMillisFormatModule() {
        super(InstantNoMillisFormatModule.class.getSimpleName(), Version.unknownVersion());
        addSerializer(Instant.class, new InstantOffsetSerializer());
        addDeserializer(Instant.class, new InstantOffsetDeserializer());
    }

    /**
     * 序列化：{@link Instant} -> 系统默认时区 offset 串，无毫秒
     **/
    public class InstantOffsetSerializer extends StdSerializer<Instant> {
        private InstantOffsetSerializer() {
            super(Instant.class);
        }

        @Override
        public void serialize(Instant value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
            OffsetDateTime odt = value.atZone(resolveDisplayZone()).toOffsetDateTime();
            gen.writeString(formatter.format(odt));
        }
    }

    /**
     * 反序列化：offset 串 -> {@link Instant}；回退兼容 ISO {@code ...Z}
     **/
    public class InstantOffsetDeserializer extends StdDeserializer<Instant> {
        private InstantOffsetDeserializer() {
            super(Instant.class);
        }

        @Override
        public Instant deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
            String str = p.getValueAsString();
            if (str == null || str.isBlank()) {
                return null;
            }
            try {
                return OffsetDateTime.parse(str, formatter).toInstant();
            } catch (DateTimeParseException dtpe) {
                // 兼容 2026-07-18T12:34:56Z / 带毫秒
                return Instant.parse(str);
            }
        }
    }

    public @NonNull ZoneId resolveDisplayZone() {
        return OS_ZONE;
    }
}
