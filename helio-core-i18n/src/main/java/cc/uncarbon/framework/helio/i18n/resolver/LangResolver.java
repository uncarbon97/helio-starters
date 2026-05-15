package cc.uncarbon.framework.helio.i18n.resolver;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Locale;
import java.util.Optional;

/**
 * 多语言解析器
 *
 * @author Uncarbon
 */
public interface LangResolver {

    /**
     * 返回解析到的 Locale，未解析到返回 empty
     */
    Optional<Locale> resolve(HttpServletRequest request);

    /**
     * 优先级，数字越小越先执行
     */
    int getOrder();

}
