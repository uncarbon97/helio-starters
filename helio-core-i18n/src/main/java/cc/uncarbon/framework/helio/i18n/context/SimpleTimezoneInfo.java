package cc.uncarbon.framework.helio.i18n.context;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.ZoneId;

/**
 * 简单多时区信息
 *
 * @author Uncarbon
 */
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
public class SimpleTimezoneInfo implements TimezoneInfo {

    @Schema(description = "IANA 时区ID", example = "Asia/Shanghai, America/New_York")
    protected String zoneIdTag;

    @Schema(description = "ZoneId 对象实例")
    protected ZoneId zoneId;

    /**
     * 例如：外显时刻按伦敦时间(UTC+0)，数据存储时刻按北京时间(UTC+8)，那么本字段就赋值为 -480
     */
    @Schema(description = "外显时刻与数据存储时刻的偏移分钟数", example = "-480")
    protected Integer timezoneOffsetMinutes;

}
