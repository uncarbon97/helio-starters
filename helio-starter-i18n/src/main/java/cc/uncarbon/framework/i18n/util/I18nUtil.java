package cc.uncarbon.framework.i18n.util;

import cc.uncarbon.framework.core.enums.HelioBaseEnum;
import cn.hutool.extra.spring.SpringUtil;
import lombok.experimental.UtilityClass;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * 获取 i18n 资源
 *
 * @author Lion Li
 * @author Uncarbon
 */
@UtilityClass
public class I18nUtil {

    private final MessageSource MESSAGE_SOURCE = SpringUtil.getBean(MessageSource.class);


    /**
     * 根据消息键和参数，获取消息
     * 支持模板填充，如: Nickname '{0}' has been existing, do you like '{1}'?
     *
     * @param code 消息代码
     * @param args 参数
     * @return 获取国际化翻译值
     */
    public String messageOf(String code, Object... args) {
        return MESSAGE_SOURCE.getMessage(code, args, LocaleContextHolder.getLocale());
    }

    /**
     * 根据消息键和参数，获取消息
     * 支持模板填充，如: Nickname '{0}' has been existing, do you like '{1}'?
     *
     * @param enumField 枚举值
     * @param args 参数
     * @return 获取国际化翻译值
     */
    public String messageOf(Enum<?> enumField, Object... args) {
        if (enumField == null) {
            return null;
        }

        return MESSAGE_SOURCE.getMessage(code, args, LocaleContextHolder.getLocale());
    }
}
