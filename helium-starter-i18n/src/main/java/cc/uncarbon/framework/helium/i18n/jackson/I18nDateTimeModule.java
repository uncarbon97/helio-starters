package cc.uncarbon.framework.helium.i18n.jackson;

import cc.uncarbon.framework.helium.i18n.context.I18nContext;
import cc.uncarbon.framework.helium.i18n.context.I18nContextHolder;
import cc.uncarbon.framework.helium.i18n.context.timezone.TimezoneInfo;
import cn.hutool.core.date.DatePattern;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.Version;
import tools.jackson.databind.JsonSerializer;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 杰克逊序列化模块：把带时区的时间类型按当前 {@link I18nContext} 的显示时区格式化输出
 *
 * <p>仅对 {@link Instant} / {@link OffsetDateTime} / {@link ZonedDateTime} 生效；
 * {@code LocalDateTime} 无时区语义，仍由 {@code DateTimeFormatModule} 按存储原样输出，避免冲突。
 *
 * <p>上下文缺失时回退到系统默认时区。通过 Jackson SPI（{@code findAndAddModules}）自动发现，
 * 因此不需要声明为 Spring Bean。
 *
 * @author Uncarbon
 */
public class I18nDateTimeModule extends SimpleModule {

    private static final DateTimeFormatter FORMATTER = DatePattern.NORM_DATETIME_FORMATTER;

    public I18nDateTimeModule() {
        super(I18nDateTimeModule.class.getSimpleName(), Version.unknownVersion());
        this.addSerializer(Instant.class, new InstantSer());
        this.addSerializer(OffsetDateTime.class, new OffsetDateTimeSer());
        this.addSerializer(ZonedDateTime.class, new ZonedDateTimeSer());
    }

    /**
     * 取当前请求的显示时区，缺失则回退到系统默认时区
     */
    private static ZoneId displayZone() {
        return I18nContextHolder.getContextOptional()
                .map(I18nContext::getTimezoneInfo)
                .map(TimezoneInfo::getZoneId)
                .orElseGet(ZoneId::systemDefault);
    }

    public static class InstantSer extends JsonSerializer<Instant> {
        @Override
        public void serialize(Instant value, JsonGenerator g, SerializationContext ctx) throws IOException {
            g.writeString(FORMATTER.format(value.atZone(displayZone())));
        }
    }

    public static class ZonedDateTimeSer extends JsonSerializer<ZonedDateTime> {
        @Override
        public void serialize(ZonedDateTime value, JsonGenerator g, SerializationContext ctx) throws IOException {
            g.writeString(FORMATTER.format(value.withZoneSameInstant(displayZone())));
        }
    }

    public static class OffsetDateTimeSer extends JsonSerializer<OffsetDateTime> {
        @Override
        public void serialize(OffsetDateTime value, JsonGenerator g, SerializationContext ctx) throws IOException {
            g.writeString(FORMATTER.format(value.atZoneSameInstant(displayZone())));
        }
    }
}
