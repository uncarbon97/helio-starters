package cc.uncarbon.framework.helium.i18n.jackson;

import cc.uncarbon.framework.helium.jackson.constant.Jackson3Constant;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * 跳过多时区换算，固定按 UTC 时区输出
 *
 * @author Uncarbon
 */
public class InstantUtcSerializer extends StdSerializer<Instant> {

    private static final ZoneId UTC_ZONE = ZoneOffset.UTC;

    private InstantUtcSerializer() {
        super(Instant.class);
    }

    @Override
    public void serialize(Instant value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
        OffsetDateTime odt = value.atZone(UTC_ZONE).toOffsetDateTime();
        DateTimeFormatter formatter = Jackson3Constant.UTC_WITH_XXX_OFFSET_FORMATTER;
        gen.writeString(formatter.format(odt));
    }
}
