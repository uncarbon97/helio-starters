package cc.uncarbon.framework.helium.i18n.resolver;

import cc.uncarbon.framework.helium.i18n.context.LangInfo;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

/**
 * 多语言解析器
 *
 * @author Uncarbon
 */
public interface LangResolver {

    /**
     * 返回解析到的 {@link LangInfo}，未解析到返回 empty
     */
    Optional<LangInfo> resolve(@NonNull HttpServletRequest servletRequest);

    /**
     * 优先级，数字越小越先执行
     */
    int getOrder();

}
