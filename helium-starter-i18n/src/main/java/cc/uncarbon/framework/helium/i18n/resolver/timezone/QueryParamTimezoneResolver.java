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
 * 从 URL 请求参数中解析时区，如「?timezone=America/New_York」
 *
 * @author Uncarbon
 */
@Slf4j
@RequiredArgsConstructor
public class QueryParamTimezoneResolver implements TimezoneResolver {

    private static final String LOG_PREFIX = HeliumI18nConstant.LOG_PREFIX + "[QueryParamTimezoneResolver]";

    static final int DEFAULT_ORDER = 10;

    private final HeliumI18nProperties props;

    @Override
    public Optional<TimezoneInfo> resolve(@NonNull HttpServletRequest servletRequest) {
        var tzCfg = props.getTimezone();
        var cfg = tzCfg != null ? tzCfg.getResolver() : null;
        // 配置缺失时跳过本解析器（交给组合器兜底系统时区），而非 NPE 被吞
        if (cfg == null || cfg.getQueryParamName() == null) {
            return Optional.empty();
        }

        String paramVal = CharSequenceUtil.cleanBlank(servletRequest.getParameter(cfg.getQueryParamName()));
        if (CharSequenceUtil.isEmpty(paramVal)) {
            return Optional.empty();
        }
        try {
            ZoneId visitorZone = ZoneId.of(paramVal);
            return Optional.of(TimezoneInfo.ofSimple(paramVal, visitorZone));
        } catch (Exception e) {
            // 值来自用户可控入参：单行 warn，不打印堆栈，避免被刷日志
            log.warn(LOG_PREFIX + " resolve failed, paramVal={}", paramVal);
        }
        return Optional.empty();
    }

    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }

}
