package cc.uncarbon.framework.helium.i18n.context.timezone;

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
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Accessors(chain = true)
public class SimpleTimezoneInfo implements TimezoneInfo {

    @Schema(description = "IANA 时区ID", example = "Asia/Shanghai, America/New_York")
    protected String zoneIdTag;

    @Schema(description = "ZoneId 对象实例")
    protected ZoneId zoneId;

}
