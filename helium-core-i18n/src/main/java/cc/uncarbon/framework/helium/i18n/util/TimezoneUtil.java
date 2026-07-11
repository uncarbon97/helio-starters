package cc.uncarbon.framework.helium.i18n.util;

import lombok.experimental.UtilityClass;
import org.jspecify.annotations.NonNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@UtilityClass
public class TimezoneUtil {

    /**
     * 计算两个 ZoneId 之间的分钟差（自动处理夏令时）
     * @param zone1 第一个时区
     * @param zone2 第二个时区
     * @return 分钟差（zone1 - zone2）
     */
    public static int getMinuteDiffBetweenZones(@NonNull final ZoneId zone1,
                                                @NonNull final ZoneId zone2) {
        // 获取当前绝对时间戳（所有时区共用）
        Instant now = Instant.now();

        // 获取两个时区在当前时刻的偏移量（秒）
        ZoneOffset offset1 = zone1.getRules().getOffset(now);
        ZoneOffset offset2 = zone2.getRules().getOffset(now);

        // 计算偏移量差值（秒），转换为分钟
        int totalSecondsDiff = offset1.getTotalSeconds() - offset2.getTotalSeconds();
        return totalSecondsDiff / 60;
    }

    /**
     * 把 {@link LocalDateTime}（在 fromZone 下解读）换算为 toZone 下的 {@link LocalDateTime}，绝对时刻不变
     */
    public static LocalDateTime convert(@NonNull final LocalDateTime src,
                                        @NonNull final ZoneId fromZone,
                                        @NonNull final ZoneId toZone) {
        return src.atZone(fromZone).withZoneSameInstant(toZone).toLocalDateTime();
    }

    /**
     * {@link LocalDateTime} + 所在时区 → 绝对 {@link Instant}
     */
    public static Instant toInstant(@NonNull final LocalDateTime localDateTime,
                                    @NonNull final ZoneId zone) {
        return localDateTime.atZone(zone).toInstant();
    }

    /**
     * 绝对 {@link Instant} → 指定时区下的 {@link LocalDateTime}
     */
    public static LocalDateTime toLocalDateTime(@NonNull final Instant instant,
                                                @NonNull final ZoneId zone) {
        return instant.atZone(zone).toLocalDateTime();
    }

    /**
     * 按 {@code TimezoneInfo.timezoneOffsetMinutes}（外显时刻 - 存储时刻）把存储侧 LocalDateTime 转为外显侧
     * <p>例如 offsetMinutes = -480（外显 UTC+0、存储 UTC+8），存储 12:00 → 外显 04:00
     */
    public static LocalDateTime storageToDisplay(@NonNull final LocalDateTime stored,
                                                 final int offsetMinutes) {
        return stored.plusMinutes(offsetMinutes);
    }

    /**
     * 按 {@code TimezoneInfo.timezoneOffsetMinutes}（外显时刻 - 存储时刻）把外显侧 LocalDateTime 转回存储侧
     * <p>例如 offsetMinutes = -480（外显 UTC+0、存储 UTC+8），外显 04:00 → 存储 12:00
     */
    public static LocalDateTime displayToStorage(@NonNull final LocalDateTime display,
                                                 final int offsetMinutes) {
        return display.minusMinutes(offsetMinutes);
    }

    /**
     * 取 {@link ZonedDateTime}（兼容 null）
     */
    public static ZonedDateTime atZone(@NonNull final Instant instant,
                                       @NonNull final ZoneId zone) {
        return instant.atZone(zone);
    }
}
