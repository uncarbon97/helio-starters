package cc.uncarbon.framework.helium.i18n.resolver.currency;

import cc.uncarbon.framework.helium.i18n.context.currency.CurrencyInfo;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

/**
 * 多币种解析器
 *
 * @author Uncarbon
 */
public interface CurrencyResolver {

    /**
     * 返回解析到的 {@link CurrencyInfo}，未解析到返回 empty
     */
    Optional<CurrencyInfo> resolve(@NonNull HttpServletRequest servletRequest);

    /**
     * 优先级，数字越小越先执行
     */
    int getOrder();

}
