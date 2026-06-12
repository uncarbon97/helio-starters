package cc.uncarbon.framework.helium.i18n.util;

import cc.uncarbon.framework.helium.base.enums.BaseEnum;
import cc.uncarbon.framework.helium.i18n.context.I18nContext;
import cc.uncarbon.framework.helium.i18n.context.I18nContextHolder;
import cc.uncarbon.framework.helium.i18n.context.LangInfo;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.extra.spring.SpringUtil;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

import java.util.Locale;
import java.util.function.Supplier;

/**
 * 国际化消息翻译工具类
 *
 * @author Lion Li@RuoYi-Vue-Plus
 * @author Uncarbon
 */
@UtilityClass
@Slf4j
public class I18nMessageUtil {

    /**
     * Slf4j 风格的占位符
     */
    private static final String SLF4J_STYLE_PLACEHOLDER = StrPool.DELIM_START + StrPool.DELIM_END;

    /**
     * Spring 提供的 {@link MessageSource} 实例
     */
    @Getter
    private MessageSource messageSource = SpringUtil.getBean(MessageSource.class);

    /**
     * {@link Locale} 实例对象提供者
     */
    private Supplier<Locale> localeSupplier = I18nMessageUtil::contextLocale;


    /**
     * 取 {@link Locale} 实例
     * 优先从 {@link I18nContextHolder} 上下文获取，兜底取系统默认值
     */
    public Locale contextLocale() {
        return I18nContextHolder.getContextOptional()
                .map(I18nContext::getLangInfo)
                .map(LangInfo::getLocale)
                .orElseGet(Locale::getDefault);
    }

    /**
     * 取 {@link Locale} 实例对象
     * 由 localeSupplier 产生返回值
     */
    public Locale determineLocale() {
        return localeSupplier.get();
    }

    /**
     * 置 {@link Locale} 实例对象提供者
     */
    public synchronized void setLocaleSupplier(Supplier<Locale> localeSupplier) {
        I18nMessageUtil.localeSupplier = localeSupplier;
    }

    /**
     * 根据消息代码和参数，获取国际化消息翻译
     * 支持模板填充，如: Nickname '{}' is already exist, do you like '{}'?
     *
     * @param code           消息代码
     * @param templateParams 模板填充参数
     * @return null or 国际化消息翻译
     */
    public String messageOf(String code, Object... templateParams) {
        return messageOf(determineLocale(), code, templateParams);
    }

    /**
     * 根据消息代码和参数，获取国际化消息翻译
     * 支持模板填充，如: Nickname '{}' is already exist, do you like '{}'?
     *
     * @param locale         locale
     * @param code           消息代码
     * @param templateParams 模板填充参数
     * @return null or 国际化消息翻译
     */
    public String messageOf(Locale locale, String code, Object... templateParams) {
        if (code == null) {
            return null;
        }

        try {
            String msg = getMessageSource().getMessage(code, null, locale != null ? locale : determineLocale());
            if (CharSequenceUtil.isEmpty(msg)) {
                return msg;
            }

            if (CharSequenceUtil.contains(msg, SLF4J_STYLE_PLACEHOLDER)) {
                // 使用 hutool 的模板填充
                return CharSequenceUtil.format(msg, templateParams);
            }

            return msg;
        } catch (NoSuchMessageException nsme) {
            // 未找到对应国际化消息翻译
        }
        return null;
    }

    /**
     * 根据消息代码和参数，获取国际化消息翻译
     * 支持模板填充，如: Nickname '{}' is already exist, do you like '{}'?
     *
     * @param code           消息代码
     * @param defaultValue   未找到对应国际化消息翻译的情况下，默认返回值
     * @param templateParams 模板填充参数
     * @return null or 国际化消息翻译
     */
    public String messageOf(String code, String defaultValue, Object... templateParams) {
        return messageOf(determineLocale(), code, defaultValue, templateParams);
    }

    /**
     * 根据消息代码和参数，获取国际化消息翻译
     * 支持模板填充，如: Nickname '{}' is already exist, do you like '{}'?
     *
     * @param locale         locale
     * @param code           消息代码
     * @param defaultValue   未找到对应国际化消息翻译的情况下，默认返回值
     * @param templateParams 模板填充参数
     * @return null or 国际化消息翻译
     */
    public String messageOf(Locale locale, String code, String defaultValue, Object... templateParams) {
        String msg = messageOf(locale, code, templateParams);
        return msg == null ? defaultValue : msg;
    }

    /**
     * 根据枚举项和参数，获取国际化消息翻译
     * 支持模板填充，如: Nickname '{}' has been existing, do you like '{}'?
     *
     * @param enumItem       枚举项
     * @param templateParams 模板填充参数
     * @return null or 国际化消息翻译
     */
    public String messageOf(Enum<?> enumItem, Object... templateParams) {
        return messageOf(determineLocale(), enumItem, templateParams);
    }

    /**
     * 根据枚举项和参数，获取国际化消息翻译
     * 支持模板填充，如: Nickname '{}' has been existing, do you like '{}'?
     *
     * @param locale         locale
     * @param enumItem       枚举项
     * @param templateParams 模板填充参数
     * @return null or 国际化消息翻译
     */
    public String messageOf(Locale locale, Enum<?> enumItem, Object... templateParams) {
        if (enumItem == null) {
            return null;
        }

        String i18nCode;

        // 尝试以较长的 枚举短名.枚举项name 为 code 尝试获取翻译值
        i18nCode = String.format("%s.%s", enumItem.getDeclaringClass().getSimpleName(), enumItem.name());
        String i18nMessage = messageOf(locale, i18nCode, templateParams);
        if (CharSequenceUtil.isNotEmpty(i18nMessage)) {
            return i18nMessage;
        }

        // 尝试以 枚举项name 为 code 尝试获取翻译值
        i18nCode = enumItem.name();
        i18nMessage = messageOf(locale, i18nCode, templateParams);
        if (CharSequenceUtil.isNotEmpty(i18nMessage)) {
            return i18nMessage;
        }

        // 以上都没找到，兜底：如果是 BaseEnum 的实现，直接返回 label 值
        if (BaseEnum.class.isAssignableFrom(enumItem.getDeclaringClass())) {
            return ((BaseEnum<?>) enumItem).getLabel();
        }

        // 最终兜底：返回枚举项 name
        return enumItem.name();
    }
}
