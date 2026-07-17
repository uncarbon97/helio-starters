package cc.uncarbon.framework.helium.i18n.resolver.timezone;

import cc.uncarbon.framework.helium.i18n.constant.HeliumI18nConstant;
import cc.uncarbon.framework.helium.i18n.context.timezone.TimezoneInfo;
import cc.uncarbon.framework.helium.i18n.props.HeliumI18nProperties;
import cn.hutool.core.collection.CollUtil;
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

    private static final String LOG_PREFIX = HeliumI18nConstant.LOG_PREFIX + "[CompositeTimezoneResolver]";

    private final HeliumI18nProperties props;
    private final List<TimezoneResolver> resolvers;

    public DefaultCompositeTimezoneResolver(HeliumI18nProperties props, List<TimezoneResolver> resolvers) {
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
                    TimezoneInfo tzInfo = tzInfoOp.get();
                    // 若显式配置了 supported-timezones，则校验；未配置则接受任意合法 ZoneId
                    if (isSupported(tzInfo.getZoneIdTag())) {
                        return tzInfo;
                    }
                }
            } catch (Exception e) {
                log.warn(LOG_PREFIX + "resolve failed, {}", e.getMessage());
            }
        }
        // 兜底返回默认时区
        String def = props.getTimezone().getOsTimezone();
        return TimezoneInfo.ofSimple(def, ZoneId.of(def));
    }

    private boolean isSupported(String zoneIdTag) {
        List<String> supported = props.getTimezone().getSupportedDisplayTimezones();
        return CollUtil.isEmpty(supported) || supported.contains(zoneIdTag);
    }
}
