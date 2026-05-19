package cc.uncarbon.framework.helio.i18n.context;

import java.util.Locale;

/**
 * 多语言信息
 *
 * @author Uncarbon
 */
public interface LangInfo {

    /**
     * 取得符合 IETF BCP 47 的语言标签
     * 例如：zh-CN, zh-TW
     */
    String getLanguageTag();

    /**
     * 取得 {@link Locale} 对象实例
     */
    Locale getLocale();

    /**
     * 快速构造一个简单的 {@link LangInfo} 实例
     */
    static LangInfo ofSimple(final String languageTag, final Locale locale) {
        return new SimpleLangInfo(languageTag, locale);
    }

}
