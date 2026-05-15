package cc.uncarbon.framework.helio.i18n.resolver.timezone;

import cc.uncarbon.framework.helio.i18n.props.HelioI18nProperties;
import cc.uncarbon.framework.helio.i18n.resolver.TimezoneResolver;
import cc.uncarbon.framework.helio.i18n.util.I18nParser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.TimeZone;

/**
 * 从 URL 请求参数中解析时区，如「?timezone=America/New_York」
 *
 * @author Uncarbon
 */
@Component
@RequiredArgsConstructor
public class QueryParamTimezoneResolver implements TimezoneResolver {

    private final HelioI18nProperties props;

    @Override
    public Optional<TimeZone> resolve(HttpServletRequest request) {
        String paramName = props.getTimezone().getResolver().getQueryParamName();
        String value = request.getParameter(paramName);
        return I18nParser.parseTimeZone(value);
    }

    @Override
    public int getOrder() {
        return 10;
    }

}
