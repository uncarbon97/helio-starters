package cc.uncarbon.framework.helium.base.errorcode;

import cn.hutool.core.text.CharSequenceUtil;

/**
 * 错误消息文本格式化策略
 *
 * @author Uncarbon
 */
public interface ErrorMessageFormatter {

    /**
     * 取错误码对应的外显消息
     *
     * @param errorCode      错误码
     * @param templateParams 模板填充参数
     * @return 消息文本
     */
    default String format(StructuredErrorCode errorCode, Object... templateParams) {
        return CharSequenceUtil.format(errorCode.getErrorMsgFriendly(), templateParams);
    }
}
