package cc.uncarbon.framework.helium.i18n.jackson;

import cc.uncarbon.framework.helium.i18n.context.I18nContext;
import cc.uncarbon.framework.helium.i18n.context.I18nContextHolder;
import cc.uncarbon.framework.helium.i18n.context.timezone.TimezoneInfo;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 把 {@link Instant} 序列化为 ISO-8601 字符串：
 * <ul>
 *     <li>能从 {@link I18nContextHolder} 取到客户端时区时，按该时区输出带偏移字符串，形如 {@code 2026-01-01T12:34:56+08:00}；</li>
 *     <li>否则（无请求上下文，如后台/异步）按 UTC 输出，形如 {@code 2026-01-01T04:34:56Z}。</li>
 * </ul>
 *
 * <p>换算走 {@code value.atZone(zone)}，按该时刻真实偏移计算，天然正确处理夏令时。
 * 客户端时区由下游工程赋值到 {@link I18nContextHolder}；
 * 无 TZ 头时 resolver 链已兜底为系统时区（{@code os-timezone}），故请求场景下上下文总是非空。
 *
 * @author Uncarbon
 */
public class TimezoneAwareInstantSerializer extends StdSerializer<Instant> {

    public static final TimezoneAwareInstantSerializer INSTANCE = new TimezoneAwareInstantSerializer();

    private static final DateTimeFormatter OFFSET_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    public TimezoneAwareInstantSerializer() {
        super(Instant.class);
    }

    @Override
    public void serialize(Instant value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
        ZoneId zone = resolveDisplayZone();
        if (zone == null) {
            gen.writeString(value.toString());
            return;
        }
        OffsetDateTime odt = value.atZone(zone).toOffsetDateTime();
        gen.writeString(odt.format(OFFSET_FORMATTER));
    }

    @Nullable
    private static ZoneId resolveDisplayZone() {
        return I18nContextHolder.getContextOptional()
                .map(I18nContext::getTimezoneInfo)
                .map(TimezoneInfo::getZoneId)
                .orElse(null);
    }
}
