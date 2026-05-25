package cc.uncarbon.framework.helium.i18n.resolver.timezone;

import cc.uncarbon.framework.helium.i18n.util.TimezoneUtil;
import cc.uncarbon.framework.helium.i18n.context.TimezoneInfo;
import cc.uncarbon.framework.helium.i18n.props.HeliumI18nProperties;
import cc.uncarbon.framework.helium.i18n.resolver.TimezoneResolver;
import cn.hutool.core.text.CharSequenceUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.Optional;

/**
 * 从 HTTP 请求头中解析时区，如「X-i18n-Timezone=America/New_York」
 *
 * @author Uncarbon
 */
@Component
@RequiredArgsConstructor
public class HeaderTimezoneResolver implements TimezoneResolver {

    static final int DEFAULT_ORDER = 20;

    private final HeliumI18nProperties props;

    @Override
    public Optional<TimezoneInfo> resolve(@NonNull HttpServletRequest servletRequest) {
        final String headerName = props.getTimezone().getResolver().getHeaderName();
        String headerVal = CharSequenceUtil.cleanBlank(servletRequest.getHeader(headerName));
        if (CharSequenceUtil.isEmpty(headerVal)) {
            return Optional.empty();
        }
        try {
            ZoneId visitorZone = ZoneId.of(headerVal);
            ZoneId defaultZone = ZoneId.of(props.getTimezone().getDefaultTimezone());
            int timezoneOffset = TimezoneUtil.getMinuteDiffBetweenZones(visitorZone, defaultZone);
            return Optional.of(TimezoneInfo.ofSimple(headerVal, visitorZone, timezoneOffset));
        } catch (Exception ignored) {}
        return Optional.empty();
    }

    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }
}
