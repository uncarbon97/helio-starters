package cc.uncarbon.framework.helio.i18n.resolver;

import cc.uncarbon.framework.helio.i18n.context.TimezoneInfo;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 组合后的多时区解析器
 */
public interface CompositeTimezoneResolver {

    /**
     * 解析为 {@link TimezoneInfo} 实例
     */
    TimezoneInfo resolve(HttpServletRequest servletRequest);

}
