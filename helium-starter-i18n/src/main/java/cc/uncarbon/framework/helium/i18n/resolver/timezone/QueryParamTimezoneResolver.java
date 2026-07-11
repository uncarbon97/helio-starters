package cc.uncarbon.framework.helium.i18n.resolver.timezone;

import cc.uncarbon.framework.helium.i18n.constant.HeliumI18nConstant;
import cc.uncarbon.framework.helium.i18n.context.timezone.TimezoneInfo;
import cc.uncarbon.framework.helium.i18n.props.HeliumI18nProperties;
import cc.uncarbon.framework.helium.i18n.util.TimezoneUtil;
import cn.hutool.core.text.CharSequenceUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import java.time.ZoneId;
import java.util.Optional;

/**
 * 从 URL 请求参数中解析时区，如「?timezone=America/New_York」
 *
 * @author Uncarbon
 */
@RequiredArgsConstructor
@Slf4j
public class QueryParamTimezoneResolver implements TimezoneResolver {

    private static final String LOG_PREFIX = HeliumI18nConstant.LOG_PREFIX + "[QueryParamTimezoneResolver]";

    static final int DEFAULT_ORDER = 10;

    private final HeliumI18nProperties props;

    @Override
    public Optional<TimezoneInfo> resolve(@NonNull HttpServletRequest servletRequest) {
        final String paramName = props.getTimezone().getResolver().getQueryParamName();
        String paramVal = CharSequenceUtil.cleanBlank(servletRequest.getParameter(paramName));
        if (CharSequenceUtil.isEmpty(paramVal)) {
            return Optional.empty();
        }
        try {
            ZoneId visitorZone = ZoneId.of(paramVal);
            ZoneId defaultZone = ZoneId.of(props.getTimezone().getDefaultTimezone());
            int timezoneOffset = TimezoneUtil.getMinuteDiffBetweenZones(visitorZone, defaultZone);
            return Optional.of(TimezoneInfo.ofSimple(paramVal, visitorZone, timezoneOffset));
        } catch (Exception e) {
            log.warn(LOG_PREFIX + " resolve failed, paramVal={}", paramVal, e);
        }
        return Optional.empty();
    }

    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }

}
