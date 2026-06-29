package cc.uncarbon.framework.helium.web.constant;

import lombok.experimental.UtilityClass;

/**
 * 预设 ServletFilter 在 Spring 容器中的 order
 *
 * @author Uncarbon
 */
@UtilityClass
public class ServletFilterOrder {

    /**
     * 用于 XssFilter
     */
    public final int XSS_FILTER = Integer.MIN_VALUE + 10000;

    /**
     * 用于 ContextBindingFilter
     */
    public final int CONTEXT_BINDING_FILTER = Integer.MIN_VALUE + 20000;

}
