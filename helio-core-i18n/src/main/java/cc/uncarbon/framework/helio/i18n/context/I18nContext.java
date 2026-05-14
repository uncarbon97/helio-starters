package cc.uncarbon.framework.helio.i18n.context;

import java.util.Locale;
import java.util.TimeZone;

/**
 * 国际化上下文
 *
 * @author Uncarbon
 */
public interface I18nContext {

    String CAMEL_NAME = "i18nContext";


    /**
     * 取得语言名称
     */
    String getLanguageTag();

    /**
     * 取得 {@link Locale} 对象实例
     */
    Locale getLocale();

    /**
     * 取得外显时刻与数据存储时刻的偏移量，单位=分钟
     * 例如：外显时刻按伦敦时间(UTC+0)，数据存储时刻按北京时间(UTC+8)，那么本字段就赋值为 -480
     */
    Integer getTimezoneOffset();

    /**
     * 取得 {@link TimeZone} 对象实例
     */
    TimeZone getTimeZone();

}
