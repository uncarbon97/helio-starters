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
@Slf4j
@RequiredArgsConstructor
public class HeaderTimezoneResolver implements TimezoneResolver {

    private static final String LOG_PREFIX = HeliumI18nConstant.LOG_PREFIX + "[HeaderTimezoneResolver]";

    static final int DEFAULT_ORDER = 20;

    private final HeliumI18nProperties props;

    @Override
    public Optional<TimezoneInfo> resolve(@NonNull HttpServletRequest servletRequest) {
        var tzCfg = props.getTimezone();
        var cfg = tzCfg != null ? tzCfg.getResolver() : null;
        // 配置缺失时跳过本解析器（交给组合器兜底系统时区），而非 NPE 被吞
        if (cfg == null || cfg.getHeaderName() == null) {
            return Optional.empty();
        }

        String headerVal = CharSequenceUtil.cleanBlank(servletRequest.getHeader(cfg.getHeaderName()));
        if (CharSequenceUtil.isEmpty(headerVal)) {
            return Optional.empty();
        }
        try {
            ZoneId visitorZone = ZoneId.of(headerVal);
            return Optional.of(TimezoneInfo.ofSimple(headerVal, visitorZone));
        } catch (Exception e) {
            // 值来自用户可控请求头：单行 warn，不打印堆栈，避免被刷日志
            log.warn(LOG_PREFIX + " resolve failed, headerVal={}", headerVal);
        }
        return Optional.empty();
    }

    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }
}
