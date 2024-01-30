package cc.uncarbon.framework.web.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.experimental.UtilityClass;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 参数绑定验证
 *
 * @author Zhu JW
 * @author Uncarbon
 **/
@UtilityClass
public class InvalidFieldUtil {

    private final Map<String, String> MESSAGE_MASKING_MAPPER = new ConcurrentHashMap<>(16);

    static {
        // Failed to convert property value of type 'java.lang.String' to required type 'java.time.LocalDateTime' for property 'beginAt'; ...
        MESSAGE_MASKING_MAPPER.put("Failed to convert property value of type", "错误参数格式或值");
    }

    /**
     * @return 返回内置的「特定关键词时-指定文案映射关系 map」
     */
    public Map<String, String> mapper() {
        return MESSAGE_MASKING_MAPPER;
    }

    /**
     * 参数绑定验证
     *
     * @return 快速返回首个无效表单项
     */
    public InvalidField getInvalidField(BindingResult bindingResult) {
        if (bindingResult == null) {
            return null;
        }

        FieldError fieldError = bindingResult.getFieldError();
        if (fieldError == null) {
            return null;
        }

        return InvalidField.builder()
                .fieldName(fieldError.getField())
                .message(maskMessage(fieldError.getDefaultMessage()))
                .build();
    }

    /**
     * 参数绑定验证
     *
     * @return 全量返回无效表单项
     */
    public List<InvalidField> listInvalidField(BindingResult bindingResult) {
        List<InvalidField> invalidFieldList = new ArrayList<>();
        if (bindingResult == null || CollUtil.isEmpty(bindingResult.getFieldErrors())) {
            // 返回空List
            return invalidFieldList;
        }

        List<FieldError> fieldErrorList = bindingResult.getFieldErrors();
        for (FieldError fieldError : fieldErrorList) {
            InvalidField invalidField = new InvalidField();
            invalidField.setFieldName(fieldError.getField());
            invalidField.setMessage(maskMessage(fieldError.getDefaultMessage()));
            invalidFieldList.add(invalidField);
        }

        return invalidFieldList;
    }

    @SuperBuilder
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class InvalidField {

        @ApiModelProperty(value = "字段名")
        private String fieldName;

        @ApiModelProperty(value = "错误原因")
        private String message;

    }

    /**
     * 为了避免详细的报错消息文本暴露给前端，当存在特定关键词时，使用指定文案返回
     *
     * @param defaultMessage 消息文本
     * @return 指定文案
     */
    private String maskMessage(String defaultMessage) {
        // corner case
        if (defaultMessage == null) {
            return CharSequenceUtil.EMPTY;
        }

        for (Entry<String, String> entry : MESSAGE_MASKING_MAPPER.entrySet()) {
            if (defaultMessage.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        // 原样返回
        return defaultMessage;
    }

}
