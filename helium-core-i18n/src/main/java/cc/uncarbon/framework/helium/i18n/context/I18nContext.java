package cc.uncarbon.framework.helium.i18n.context;

import cc.uncarbon.framework.helium.i18n.context.currency.CurrencyInfo;
import cc.uncarbon.framework.helium.i18n.context.lang.LangInfo;
import cc.uncarbon.framework.helium.i18n.context.timezone.TimezoneInfo;

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

    /**
     * 取得 {@link CurrencyInfo} 实例
     */
    CurrencyInfo getCurrencyInfo();

}
