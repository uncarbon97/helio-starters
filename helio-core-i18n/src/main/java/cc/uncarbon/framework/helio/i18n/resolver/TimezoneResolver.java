package cc.uncarbon.framework.helio.i18n.resolver;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;
import java.util.TimeZone;

/**
 * 多时区解析器
 *
 * @author Uncarbon
 */
public interface TimezoneResolver {

    /**
     * 返回解析到的 TimeZone，未解析到返回 empty
     */
    Optional<TimeZone> resolve(HttpServletRequest request);

    /**
     * 优先级，数字越小越先执行
     */
    int getOrder();

}
