package cc.uncarbon.framework.helio.i18n.resolver.timezone;

import cc.uncarbon.framework.helio.i18n.constant.HelioI18nConstant;
import cc.uncarbon.framework.helio.i18n.context.TimezoneInfo;
import cc.uncarbon.framework.helio.i18n.props.HelioI18nProperties;
import cc.uncarbon.framework.helio.i18n.resolver.CompositeTimezoneResolver;
import cc.uncarbon.framework.helio.i18n.resolver.TimezoneResolver;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * 组合多时区解析器，按 order 升序尝试各解析器，返回第一个有效结果
 *
 * @author Uncarbon
 */
@Slf4j
public class DefaultCompositeTimezoneResolver implements CompositeTimezoneResolver {

    private static final String LOG_PREFIX = HelioI18nConstant.LOG_PREFIX + "[CompositeTimezoneResolver]";

    private final HelioI18nProperties props;
    private final List<TimezoneResolver> resolvers;

    public DefaultCompositeTimezoneResolver(HelioI18nProperties props, List<TimezoneResolver> resolvers) {
        this.props = props;
        this.resolvers = resolvers.stream()
                .sorted(Comparator.comparingInt(TimezoneResolver::getOrder))
                .toList();
    }

    @Override
    public TimezoneInfo resolve(HttpServletRequest servletRequest) {
        for (TimezoneResolver resolver : resolvers) {
            try {
                Optional<TimezoneInfo> tzInfoOp = resolver.resolve(servletRequest);
                if (tzInfoOp.isPresent()) {
                    return tzInfoOp.get();
                }
            } catch (Exception e) {
                log.warn(LOG_PREFIX + "resolve failed", e);
            }
        }
        // 兜底返回默认时区
        String defaultTimezone = props.getTimezone().getDefaultTimezone();
        return TimezoneInfo.ofSimple(defaultTimezone, ZoneId.of(defaultTimezone), 0);
    }
}
