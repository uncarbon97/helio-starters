package cc.uncarbon.framework.web.config;

import cc.uncarbon.framework.core.constant.HelioConstant;
import cc.uncarbon.framework.core.exception.BusinessException;
import cc.uncarbon.framework.core.props.HelioProperties;
import cc.uncarbon.framework.i18n.util.I18nUtil;
import cc.uncarbon.framework.web.enums.GlobalWebExceptionI18nMessageEnum;
import cc.uncarbon.framework.web.model.response.ApiResult;
import cc.uncarbon.framework.web.util.InvalidFieldUtil;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.fasterxml.jackson.core.JsonParseException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;

/**
 * Web 全局异常处理自动配置类
 *
 * @author Uncarbon
 */
@ConditionalOnMissingBean(value = GlobalWebExceptionHandlerAutoConfiguration.class)
@RequiredArgsConstructor
@AutoConfiguration
@ControllerAdvice
@RestController
@Slf4j
public class GlobalWebExceptionHandlerAutoConfiguration {

    protected static final MediaType MEDIA_TYPE = new MediaType("application", "json", StandardCharsets.UTF_8);
    protected static final String DUBBO_PACKAGE_PREFIX = "org.apache.dubbo";
    private final HelioProperties helioProperties;


    /**
     * 主动抛出的业务异常
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({BusinessException.class})
    public ResponseEntity<ApiResult<?>> handleBusinessException(BusinessException e, HttpServletRequest request) {
        this.logError(e, request);

        /*
        @since 1.7.2，国际化支持；详见文档：进阶使用-国际化
         */
        String msg;
        if (helioProperties.getI18n().getEnabled() && e.getCustomEnumField() != null) {
            // 如果启用了国际化，并制定了枚举值，则按国际化翻译值显示
            msg = I18nUtil.messageOf(e.getCustomEnumField(), e.getTemplateParams());
        } else {
            // 按已有值显示
            msg = e.getMessage();
        }

        ApiResult<?> ret = ApiResult.fail(e.getCode(), msg);
        return createResponseEntity(HttpStatus.BAD_REQUEST, ret);
    }

    /**
     * 用户登录异常
     */
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler({NotLoginException.class})
    public ResponseEntity<ApiResult<?>> handleNotLoginException(NotLoginException e, HttpServletRequest request) {
        this.logError(e, request);

        GlobalWebExceptionI18nMessageEnum msgEnum = GlobalWebExceptionI18nMessageEnum.GLOBAL__NO_LOGIN;
        ApiResult<?> ret = ApiResult.fail(HttpStatus.UNAUTHORIZED.value(), this.getI18nMessage(msgEnum));
        return createResponseEntity(HttpStatus.UNAUTHORIZED, ret);
    }

    /**
     * 用户权限异常
     */
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler({NotPermissionException.class})
    public ResponseEntity<ApiResult<?>> handleNotPermissionException(NotPermissionException e, HttpServletRequest request) {
        this.logError(e, request);

        GlobalWebExceptionI18nMessageEnum msgEnum = GlobalWebExceptionI18nMessageEnum.GLOBAL__PERMISSION_NOT_MATCH;
        ApiResult<?> ret = ApiResult.fail(HttpStatus.FORBIDDEN.value(), this.getI18nMessage(msgEnum));
        return createResponseEntity(HttpStatus.FORBIDDEN, ret);
    }

    /**
     * 用户角色异常
     */
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler({NotRoleException.class})
    public ResponseEntity<ApiResult<?>> handleNotRoleException(NotRoleException e, HttpServletRequest request) {
        this.logError(e, request);

        GlobalWebExceptionI18nMessageEnum msgEnum = GlobalWebExceptionI18nMessageEnum.GLOBAL__ROLE_NOT_MATCH;
        ApiResult<?> ret = ApiResult.fail(HttpStatus.FORBIDDEN.value(), this.getI18nMessage(msgEnum));
        return createResponseEntity(HttpStatus.FORBIDDEN, ret);
    }

    /**
     * 404 NOT FOUND
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({NoHandlerFoundException.class})
    public ResponseEntity<ApiResult<?>> handleNoHandlerFoundException(NoHandlerFoundException e, HttpServletRequest request) {
        this.logError(e, request);

        GlobalWebExceptionI18nMessageEnum msgEnum = GlobalWebExceptionI18nMessageEnum.GLOBAL__NOT_FOUND;
        ApiResult<?> ret = ApiResult.fail(HttpStatus.NOT_FOUND.value(), this.getI18nMessage(msgEnum));
        return createResponseEntity(HttpStatus.NOT_FOUND, ret);
    }

    /**
     * JsonParseException, HttpMessageNotReadableException
     * Jackson反序列化异常
     * 通常是因为JSON格式错误，或枚举输入值超出范围
     * <p>
     * IllegalArgumentException
     * 不合法的参数异常
     * <p>
     * MethodArgumentTypeMismatchException
     * "@PathVariable" 注解收参类型为 Long，但传的是 String
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({JsonParseException.class, HttpMessageNotReadableException.class, IllegalArgumentException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiResult<?>> handleJsonParseException(Exception e, HttpServletRequest request) {
        this.logError(e, request);

        GlobalWebExceptionI18nMessageEnum msgEnum = GlobalWebExceptionI18nMessageEnum.GLOBAL__UNACCEPTABLE_PARAMETERS;
        ApiResult<?> ret = ApiResult.fail(HttpStatus.NOT_ACCEPTABLE.value(), this.getI18nMessage(msgEnum));
        return createResponseEntity(HttpStatus.NOT_ACCEPTABLE, ret);
    }

    /**
     * JSR303 表单参数校验失败，或入参格式转换失败
     * 需在 Controller 层使用@Valid注解
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ApiResult<?>> handleBindException(BindException e, HttpServletRequest request) {
        this.logError(e, request);

        GlobalWebExceptionI18nMessageEnum msgEnum = GlobalWebExceptionI18nMessageEnum.GLOBAL__UNACCEPTABLE_PARAMETERS;
        ApiResult<?> ret = ApiResult.fail(HttpStatus.NOT_ACCEPTABLE.value(), this.getI18nMessage(msgEnum), InvalidFieldUtil.getInvalidField(e.getBindingResult()));
        return createResponseEntity(HttpStatus.NOT_ACCEPTABLE, ret);
    }

    /**
     * 请求方式不对, 比如POST接口用了GET请求
     */
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler({HttpRequestMethodNotSupportedException.class})
    public ResponseEntity<ApiResult<?>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        this.logError(e, request);

        GlobalWebExceptionI18nMessageEnum msgEnum = GlobalWebExceptionI18nMessageEnum.GLOBAL__METHOD_NOT_ALLOWED;
        ApiResult<?> ret = ApiResult.fail(HttpStatus.METHOD_NOT_ALLOWED.value(), this.getI18nMessage(msgEnum));
        return createResponseEntity(HttpStatus.METHOD_NOT_ALLOWED, ret);
    }

    /**
     * 兜底未归类异常
     * 如ClientException(Dubbo RPC), RpcException(Dubbo RPC), SQLException, RuntimeException一类的都会掉到这里来, 并打印堆栈
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({Exception.class})
    public ResponseEntity<ApiResult<?>> handleException(Exception e, HttpServletRequest request) {
        // 打印堆栈，方便溯源
        this.logError(e, request, true);

        int responseCode = HttpStatus.INTERNAL_SERVER_ERROR.value();

        if (e.getClass().getName().startsWith(DUBBO_PACKAGE_PREFIX)) {
            // Dubbo RPC异常
            responseCode = HelioConstant.Dubbo.RPC_EXCEPTION_RESPONSE_CODE;
        }

        GlobalWebExceptionI18nMessageEnum msgEnum = GlobalWebExceptionI18nMessageEnum.GLOBAL__INTERNAL_ERROR;
        ApiResult<?> ret = ApiResult.fail(responseCode, this.getI18nMessage(msgEnum));
        return createResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, ret);
    }

    /*
    ----------------------------------------------------------------
                        私有方法 private methods
    ----------------------------------------------------------------
     */

    protected void logError(Exception e, HttpServletRequest request) {
        logError(e, request, false);
    }

    protected void logError(Exception e, HttpServletRequest request, boolean printExceptionStack) {
        log.error("[Web][有异常被抛出] >> 异常类=[{}], URI=[{}], 消息=[{}]  {}", e.getClass().getName(), request.getRequestURI(), e.getMessage(), printExceptionStack ? e : null);
    }

    protected static ResponseEntity<ApiResult<?>> createResponseEntity(HttpStatus httpStatus, ApiResult<?> body) {
        return ResponseEntity.status(httpStatus.value()).contentType(MEDIA_TYPE).body(body);
    }

    /**
     * 取国际化翻译值或默认消息，取决于是否实际启用了国际化功能
     * @param msgEnum 全局异常处理国际化消息枚举
     * @return 消息文本
     */
    protected String getI18nMessage(@NonNull GlobalWebExceptionI18nMessageEnum msgEnum) {
        if (helioProperties.getI18n().getEnabled()) {
            return I18nUtil.messageOf(msgEnum.i18nCode(), msgEnum.getDefaultValue());
        }
        return msgEnum.getDefaultValue();
    }
}
