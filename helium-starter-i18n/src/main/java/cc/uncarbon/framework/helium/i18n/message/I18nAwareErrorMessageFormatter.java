package cc.uncarbon.framework.helium.i18n.message;

import cc.uncarbon.framework.helium.base.errorcode.ErrorMessageFormatter;
import cc.uncarbon.framework.helium.base.errorcode.StructuredErrorCode;
import cc.uncarbon.framework.helium.i18n.util.I18nMessageUtil;

/**
 * 经 {@link I18nMessageUtil} 取翻译值，兜底回退到友好文案
 *
 * @author Uncarbon
 */
public class I18nAwareErrorMessageFormatter implements ErrorMessageFormatter {

    @Override
    public String format(StructuredErrorCode errorCode, Object... templateParams) {
        return I18nMessageUtil.messageOf(errorCode.getErrorCode(), errorCode.getErrorMsgFriendly(), templateParams);
    }
}
