package cc.uncarbon.framework.helium.i18n.context;

import java.time.ZoneId;

/**
 * 多时区信息
 *
 * @author Uncarbon
 */
public interface TimezoneInfo {

    /**
     * 取得 IANA 时区ID
     * 例如：Asia/Shanghai, America/New_York
     */
    String getZoneIdTag();

    /**
     * 取得 {@link ZoneId} 对象实例
     */
    ZoneId getZoneId();

    /**
     * 取得外显时刻与数据存储时刻的偏移分钟数
     * 例如：外显时刻按伦敦时间(UTC+0)，数据存储时刻按北京时间(UTC+8)，那么本字段就赋值为 -480
     */
    Integer getTimezoneOffsetMinutes();

    /**
     * 快速构造一个简单的 {@link TimezoneInfo} 实例
     */
    static TimezoneInfo ofSimple(final String zoneIdTag, final ZoneId zoneId, final Integer timezoneOffsetMinutes) {
        return new SimpleTimezoneInfo(zoneIdTag, zoneId, timezoneOffsetMinutes);
    }

}
