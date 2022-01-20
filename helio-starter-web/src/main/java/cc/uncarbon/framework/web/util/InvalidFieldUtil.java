package cc.uncarbon.framework.web.util;

import cn.hutool.core.collection.CollUtil;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.experimental.UtilityClass;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

/**
 * 参数绑定验证
 *
 * @author Zhu JW
 * @author Uncarbon
 **/
@UtilityClass
public class InvalidFieldUtil {

    /**
     * 参数绑定验证
     * @return 快速返回首个无效表单项
     */
    public static InvalidField getInvalidField(BindingResult bindingResult) {
        if (bindingResult == null) {
            return null;
        }

        FieldError fieldError = bindingResult.getFieldError();
        if (fieldError == null) {
            return null;
        }

        return InvalidField.builder()
                .fieldName(fieldError.getField())
                .message(fieldError.getDefaultMessage())
                .build();
    }

    /**
     * 参数绑定验证
     * @return 全量返回无效表单项
     */
    public static List<InvalidField> listInvalidField(BindingResult bindingResult) {
        List<InvalidField> invalidFieldList = new ArrayList<>();
        if (bindingResult == null || CollUtil.isEmpty(bindingResult.getFieldErrors())) {
            // 返回空List
            return invalidFieldList;
        }

        List<FieldError> fieldErrorList = bindingResult.getFieldErrors();
        for (FieldError fieldError : fieldErrorList) {
            InvalidField invalidField = new InvalidField();
            invalidField.setFieldName(fieldError.getField());
            invalidField.setMessage(fieldError.getDefaultMessage());
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

}
