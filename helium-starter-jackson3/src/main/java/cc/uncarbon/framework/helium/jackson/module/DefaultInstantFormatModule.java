package cc.uncarbon.framework.helium.jackson.module;

import cn.hutool.core.date.DatePattern;
import tools.jackson.core.Version;
import tools.jackson.databind.ext.javatime.deser.LocalDateDeserializer;
import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer;
import tools.jackson.databind.ext.javatime.deser.LocalTimeDeserializer;
import tools.jackson.databind.ext.javatime.ser.InstantSerializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateSerializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;
import tools.jackson.databind.ext.javatime.ser.LocalTimeSerializer;
import tools.jackson.databind.module.SimpleModule;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 仅处理视为纯本地语义的 {@link LocalDateTime}/{@link LocalDate}/{@link LocalTime}
 * {@link java.time.Instant} 的（多时区）序列化由 i18n 模块经 SPI 注册的 module 提供，
 * 未引入 i18n 时回退到 Jackson 内建输出。
 *
 * @author Uncarbon
 **/
public class DefaultInstantFormatModule extends SimpleModule {

    public DefaultInstantFormatModule() {
        super(DefaultInstantFormatModule.class.getSimpleName(), Version.unknownVersion());
        InstantSerializer
        this.addSerializer(Instant.class, InstantSerializer.INSTANCE);
    }
}
