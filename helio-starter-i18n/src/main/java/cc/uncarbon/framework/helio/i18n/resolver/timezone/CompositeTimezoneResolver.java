package cc.uncarbon.framework.helio.i18n.resolver.timezone;

import cc.uncarbon.framework.helio.i18n.props.HelioI18nProperties;
import cc.uncarbon.framework.helio.i18n.resolver.TimezoneResolver;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

/**
 * 组合时区解析器，按 order 升序尝试各解析器，返回第一个有效结果
 *
 * @author Uncarbon
 */
@Slf4j
public class CompositeTimezoneResolver {

    private final List<TimezoneResolver> resolvers;
    private final HelioI18nProperties props;

    public CompositeTimezoneResolver(HelioI18nProperties props, List<TimezoneResolver> resolvers) {
        this.props = props;
        this.resolvers = resolvers.stream()
                .sorted(Comparator.comparingInt(TimezoneResolver::getOrder))
                .toList();
    }

    public TimeZone resolve(HttpServletRequest request) {
        for (TimezoneResolver resolver : resolvers) {
            try {
                Optional<TimeZone> tz = resolver.resolve(request);
                if (tz.isPresent()) {
                    return tz.get();
                }
            } catch (Exception e) {
                log.warn("Timezone resolver {} failed", resolver.getClass().getSimpleName(), e);
            }
        }
        return TimeZone.getTimeZone(ZoneId.of(props.getTimezone().getDefaultTimezone()));
    }
}
