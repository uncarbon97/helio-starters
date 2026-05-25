package cc.uncarbon.framework.helium.i18n.context;

/**
 * 国际化上下文
 *
 * @author Uncarbon
 */
public interface I18nContext {

    String CAMEL_NAME = "i18nContext";

    /**
     * 取得 {@link LangInfo} 实例
     */
    LangInfo getLangInfo();

    /**
     * 取得 {@link TimezoneInfo} 实例
     */
    TimezoneInfo getTimezoneInfo();

}
