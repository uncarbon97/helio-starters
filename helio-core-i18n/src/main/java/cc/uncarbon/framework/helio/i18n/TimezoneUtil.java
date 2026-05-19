package cc.uncarbon.framework.helio.i18n;

import lombok.experimental.UtilityClass;
import org.jspecify.annotations.NonNull;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

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
}
