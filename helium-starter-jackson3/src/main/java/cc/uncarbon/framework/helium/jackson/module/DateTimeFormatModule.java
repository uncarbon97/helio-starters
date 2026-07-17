package cc.uncarbon.framework.helium.jackson.module;

import cn.hutool.core.date.DatePattern;
import tools.jackson.core.Version;
import tools.jackson.databind.ext.javatime.deser.LocalDateDeserializer;
import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer;
import tools.jackson.databind.ext.javatime.deser.LocalTimeDeserializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateSerializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;
import tools.jackson.databind.ext.javatime.ser.LocalTimeSerializer;
import tools.jackson.databind.module.SimpleModule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 仅处理视为纯本地语义的 {@link LocalDateTime}/{@link LocalDate}/{@link LocalTime}
 *
 * @author Uncarbon
 **/
public class DateTimeFormatModule extends SimpleModule {

    public DateTimeFormatModule() {
        this(DatePattern.NORM_DATETIME_FORMATTER, DatePattern.NORM_DATE_FORMATTER, DatePattern.NORM_TIME_FORMATTER);
    }

    /**
     * 可分别指定时间格式
     *
     * @param dateTimeFormatter 日期+时间格式
     * @param dateFormatter     仅日期格式
     * @param timeFormatter     仅时间格式
     */
    public DateTimeFormatModule(DateTimeFormatter dateTimeFormatter, DateTimeFormatter dateFormatter,
                                DateTimeFormatter timeFormatter) {
        super(DateTimeFormatModule.class.getSimpleName(), Version.unknownVersion());
        this.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
        this.addSerializer(LocalDate.class, new LocalDateSerializer(dateFormatter));
        this.addSerializer(LocalTime.class, new LocalTimeSerializer(timeFormatter));
        this.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));
        this.addDeserializer(LocalDate.class, new LocalDateDeserializer(dateFormatter));
        this.addDeserializer(LocalTime.class, new LocalTimeDeserializer(timeFormatter));
    }
}
