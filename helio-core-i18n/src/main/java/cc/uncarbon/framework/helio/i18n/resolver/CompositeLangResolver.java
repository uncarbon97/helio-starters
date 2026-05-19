package cc.uncarbon.framework.helio.i18n.resolver;

import cc.uncarbon.framework.helio.i18n.context.LangInfo;
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
