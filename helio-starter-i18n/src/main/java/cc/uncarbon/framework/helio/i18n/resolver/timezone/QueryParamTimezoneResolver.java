package cc.uncarbon.framework.helio.i18n.resolver.timezone;

import cc.uncarbon.framework.helio.i18n.util.TimezoneUtil;
import cc.uncarbon.framework.helio.i18n.context.TimezoneInfo;
import cc.uncarbon.framework.helio.i18n.props.HelioI18nProperties;
import cc.uncarbon.framework.helio.i18n.resolver.TimezoneResolver;
import cn.hutool.core.text.CharSequenceUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.Optional;

/**
 * 从 URL 请求参数中解析时区，如「?timezone=America/New_York」
 *
 * @author Uncarbon
 */
@Component
@RequiredArgsConstructor
public class QueryParamTimezoneResolver implements TimezoneResolver {

    static final int DEFAULT_ORDER = 10;

    private final HelioI18nProperties props;

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
        } catch (Exception ignored) {}
        return Optional.empty();
    }

    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }

}
