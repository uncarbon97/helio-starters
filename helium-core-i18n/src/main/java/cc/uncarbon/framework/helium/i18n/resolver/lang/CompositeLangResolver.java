package cc.uncarbon.framework.helium.i18n.resolver.lang;

import cc.uncarbon.framework.helium.i18n.context.lang.LangInfo;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 组合后的多语言解析器
 */
public interface CompositeLangResolver {

    /**
     * 解析为 {@link LangInfo} 实例
     */
    LangInfo resolve(HttpServletRequest servletRequest);

}
