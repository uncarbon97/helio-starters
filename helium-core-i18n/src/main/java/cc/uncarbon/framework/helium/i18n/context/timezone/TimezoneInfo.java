package cc.uncarbon.framework.helium.i18n.context.timezone;

import java.time.ZoneId;

/**
 * 多时区信息
 *
 * @author Uncarbon
 */
public interface TimezoneInfo {

    /**
     * 取得 IANA 时区 ID
     * 例如：Asia/Shanghai, America/New_York
     */
    String getZoneIdTag();

    /**
     * 取得 {@link ZoneId} 对象实例
     */
    ZoneId getZoneId();

    /**
     * 快速构造一个简单的 {@link TimezoneInfo} 实例
     */
    static TimezoneInfo ofSimple(final String zoneIdTag, final ZoneId zoneId) {
        return new SimpleTimezoneInfo(zoneIdTag, zoneId);
    }

}
