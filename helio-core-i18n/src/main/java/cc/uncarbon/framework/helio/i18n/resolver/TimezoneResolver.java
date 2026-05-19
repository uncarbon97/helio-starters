package cc.uncarbon.framework.helio.i18n.resolver;

import cc.uncarbon.framework.helio.i18n.context.TimezoneInfo;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

/**
 * 多时区解析器
 *
 * @author Uncarbon
 */
public interface TimezoneResolver {

    /**
     * 返回解析到的 {@link TimezoneInfo}，未解析到返回 empty
     */
    Optional<TimezoneInfo> resolve(@NonNull HttpServletRequest servletRequest);

    /**
     * 优先级，数字越小越先执行
     */
    int getOrder();

}
