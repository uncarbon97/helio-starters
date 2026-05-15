package cc.uncarbon.framework.helio.i18n.resolver.timezone;

import cc.uncarbon.framework.helio.i18n.props.HelioI18nProperties;
import cc.uncarbon.framework.helio.i18n.resolver.TimezoneResolver;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.TimeZone;

/**
 * 从 HTTP 请求头中解析时区，如「X-i18n-Timezone=America/New_York」
 *
 * @author Uncarbon
 */
@Component
@RequiredArgsConstructor
public class HeaderTimezoneResolver implements TimezoneResolver {

    private final HelioI18nProperties properties;

    @Override
    public Optional<TimeZone> resolve(HttpServletRequest request) {
        String headerName = properties.getTimezone().getResolver().getHeaderName();
        String value = request.getHeader(headerName);
        return QueryParamTimezoneResolver.parseTimeZoneQuietly(value);
    }

    @Override
    public int getOrder() {
        return 40;
    }
}
