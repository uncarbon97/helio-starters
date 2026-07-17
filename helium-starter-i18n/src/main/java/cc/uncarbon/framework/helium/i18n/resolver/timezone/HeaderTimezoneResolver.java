package cc.uncarbon.framework.helium.i18n.resolver.timezone;

import cc.uncarbon.framework.helium.i18n.constant.HeliumI18nConstant;
import cc.uncarbon.framework.helium.i18n.context.timezone.TimezoneInfo;
import cc.uncarbon.framework.helium.i18n.props.HeliumI18nProperties;
import cn.hutool.core.text.CharSequenceUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import java.time.ZoneId;
import java.util.Optional;

/**
 * 从 HTTP 请求头中解析时区，如「X-i18n-Timezone=America/New_York」
 *
 * @author Uncarbon
 */
@RequiredArgsConstructor
@Slf4j
public class HeaderTimezoneResolver implements TimezoneResolver {

    private static final String LOG_PREFIX = HeliumI18nConstant.LOG_PREFIX + "[HeaderTimezoneResolver]";

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
            return Optional.of(TimezoneInfo.ofSimple(headerVal, visitorZone));
        } catch (Exception e) {
            log.warn(LOG_PREFIX + " resolve failed, headerVal={}", headerVal, e);
        }
        return Optional.empty();
    }

    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }
}
