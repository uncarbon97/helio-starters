package cc.uncarbon.framework.helio.web.handler;

import cc.uncarbon.framework.helio.base.exception.BusinessException;
import cc.uncarbon.framework.helio.web.enums.GlobalWebExceptionFriendlyMessageEnum;
import cc.uncarbon.framework.helio.web.model.response.ApiResult;
import cc.uncarbon.framework.helio.web.util.InvalidFieldUtil;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.json.JsonParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.nio.charset.StandardCharsets;

/**
 * Web 全局异常处理自动配置类
 *
 * @author Uncarbon
 */
@ConditionalOnMissingBean
@RequiredArgsConstructor
@ControllerAdvice
@Slf4j
public class GlobalWebExceptionHandler {

    protected static final MediaType MEDIA_TYPE_APPLICATION_JSON_UTF8 =
            new MediaType("application", "json", StandardCharsets.UTF_8);
    private static final String LOG_PREFIX = "[Web]";


    /**
     * 主动抛出的业务异常
     */
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = {BusinessException.class})
    public ResponseEntity<ApiResult<Void>> handleBusinessException(BusinessException e, HttpServletRequest servletRequest) {
        this.logException(e, servletRequest);

        String msg = e.getMessage();
        ApiResult<Void> ret;
        Integer codeAsInt = e.getCodeAsInt();
        if (codeAsInt != null) {
            ret = ApiResult.fail(codeAsInt, msg);
        } else {
            String codeAsString = e.getCodeAsString();
            ret = ApiResult.fail(codeAsString, msg);
        }
        return createResponseEntity(HttpStatus.BAD_REQUEST, ret);
    }

    /**
     * 用户登录异常
     */
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(value = {NotLoginException.class})
    public ResponseEntity<ApiResult<Void>> handleNotLoginException(NotLoginException e, HttpServletRequest servletRequest) {
        this.logException(e, servletRequest);

        GlobalWebExceptionFriendlyMessageEnum msgEnum = GlobalWebExceptionFriendlyMessageEnum.NO_LOGIN;
        ApiResult<Void> ret = ApiResult.fail(HttpStatus.UNAUTHORIZED.value(), this.determineI18nMessage(msgEnum));
        return createResponseEntity(HttpStatus.UNAUTHORIZED, ret);
    }

    /**
     * 用户权限异常
     */
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    @ExceptionHandler(value = {NotPermissionException.class})
    public ResponseEntity<ApiResult<Void>> handleNotPermissionException(NotPermissionException e,
                                                                        HttpServletRequest servletRequest) {
        this.logException(e, servletRequest);

        GlobalWebExceptionFriendlyMessageEnum msgEnum = GlobalWebExceptionFriendlyMessageEnum.NO_PERMISSION;
        ApiResult<Void> ret = ApiResult.fail(HttpStatus.FORBIDDEN.value(), this.determineI18nMessage(msgEnum));
        return createResponseEntity(HttpStatus.FORBIDDEN, ret);
    }

    /**
     * 用户角色异常
     */
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    @ExceptionHandler(value = {NotRoleException.class})
    public ResponseEntity<ApiResult<Void>> handleNotRoleException(NotRoleException e, HttpServletRequest servletRequest) {
        this.logException(e, servletRequest);

        GlobalWebExceptionFriendlyMessageEnum msgEnum = GlobalWebExceptionFriendlyMessageEnum.NO_ROLE;
        ApiResult<Void> ret = ApiResult.fail(HttpStatus.FORBIDDEN.value(), this.determineI18nMessage(msgEnum));
        return createResponseEntity(HttpStatus.FORBIDDEN, ret);
    }

    /**
     * 404 异常
     */
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ExceptionHandler(value = {NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ApiResult<Void>> handleNoHandlerFoundException(Exception e, HttpServletRequest servletRequest) {
        this.logException(e, servletRequest);

        GlobalWebExceptionFriendlyMessageEnum msgEnum = GlobalWebExceptionFriendlyMessageEnum.NOT_FOUND_404;
        ApiResult<Void> ret = ApiResult.fail(HttpStatus.NOT_FOUND.value(), this.determineI18nMessage(msgEnum));
        return createResponseEntity(HttpStatus.NOT_FOUND, ret);
    }

    /**
     * JsonParseException, HttpMessageNotReadableException
     * Jackson 反序列化异常
     * 通常是因为 JSON 格式错误，或枚举输入值超出范围
     * <p>
     * IllegalArgumentException
     * 不合法的参数异常
     * <p>
     * MethodArgumentTypeMismatchException
     * "@PathVariable" 注解收参类型为 Long，但传的是 String
     */
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = {JsonParseException.class, HttpMessageNotReadableException.class,
            IllegalArgumentException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiResult<Void>> handleJsonParseException(Exception e, HttpServletRequest servletRequest) {
        this.logException(e, servletRequest);

        GlobalWebExceptionFriendlyMessageEnum msgEnum = GlobalWebExceptionFriendlyMessageEnum.NOT_ACCEPTABLE_INPUT;
        ApiResult<Void> ret = ApiResult.fail(HttpStatus.NOT_ACCEPTABLE.value(), this.determineI18nMessage(msgEnum));
        return createResponseEntity(HttpStatus.NOT_ACCEPTABLE, ret);
    }

    /**
     * JSR303 表单参数校验失败，或入参格式转换失败
     * 需在 Controller 层使用@Valid注解
     */
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = {MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ApiResult<InvalidFieldUtil.InvalidField>> handleBindException(BindException e,
                                                                                        HttpServletRequest servletRequest) {
        this.logException(e, servletRequest);

        GlobalWebExceptionFriendlyMessageEnum msgEnum = GlobalWebExceptionFriendlyMessageEnum.NOT_ACCEPTABLE_INPUT;
        ApiResult<InvalidFieldUtil.InvalidField> ret =
                ApiResult.fail(HttpStatus.NOT_ACCEPTABLE.value(), this.determineI18nMessage(msgEnum), InvalidFieldUtil.getInvalidField(e.getBindingResult()));
        return createResponseEntity(HttpStatus.NOT_ACCEPTABLE, ret);
    }

    /**
     * 请求方式不对
     * HttpRequestMethodNotSupportedException 如：POST接口用了GET请求
     * HttpMediaTypeNotSupportedException 如：Content-type 应为 application/json 的，使用了 text/plain
     */
    @ResponseStatus(value = HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(value = {HttpRequestMethodNotSupportedException.class, HttpMediaTypeNotSupportedException.class})
    public ResponseEntity<ApiResult<Void>> handleServletException(ServletException e,
                                                                  HttpServletRequest servletRequest) {
        this.logException(e, servletRequest);

        GlobalWebExceptionFriendlyMessageEnum msgEnum = GlobalWebExceptionFriendlyMessageEnum.METHOD_NOT_ALLOWED;
        ApiResult<Void> ret = ApiResult.fail(HttpStatus.METHOD_NOT_ALLOWED.value(), this.determineI18nMessage(msgEnum));
        return createResponseEntity(HttpStatus.METHOD_NOT_ALLOWED, ret);
    }

    /**
     * 兜底未归类异常，如：
     * ClientException(Dubbo RPC)
     * RpcException(Dubbo RPC)
     * SQLException
     * RuntimeException
     * 一类的都会落到这里来，并打印堆栈
     */
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<ApiResult<Void>> handleException(Exception e, HttpServletRequest servletRequest) {
        // 打印堆栈，方便溯源
        this.logException(e, servletRequest, true);

        GlobalWebExceptionFriendlyMessageEnum msgEnum = GlobalWebExceptionFriendlyMessageEnum.INTERNAL_SERVER_ERROR;
        ApiResult<Void> ret = ApiResult.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), this.determineI18nMessage(msgEnum));
        return createResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, ret);
    }

    /*
    ----------------------------------------------------------------
                        私有方法 private methods
    ----------------------------------------------------------------
     */

    protected void logException(BusinessException e, HttpServletRequest servletRequest) {
        log.error(LOG_PREFIX + "[业务异常] {} >> URI=[{}]", e.getMessage(), servletRequest.getRequestURI());
    }

    protected void logException(Exception e, HttpServletRequest servletRequest) {
        logException(e, servletRequest, false);
    }

    protected void logException(Exception e, HttpServletRequest servletRequest, boolean printExceptionStack) {
        if (printExceptionStack) {
            log.error(LOG_PREFIX + "[非业务异常] >> 异常类=[{}], URI=[{}], 消息=[{}]",
                    e.getClass().getName(), servletRequest.getRequestURI(), e.getMessage(), e);
            return;
        }
        log.error(LOG_PREFIX + "[非业务异常] >> 异常类=[{}], URI=[{}], 消息=[{}]",
                e.getClass().getName(), servletRequest.getRequestURI(), e.getMessage());
    }

    protected static <T> ResponseEntity<ApiResult<T>> createResponseEntity(HttpStatus httpStatus, ApiResult<T> body) {
        return ResponseEntity.status(httpStatus.value()).contentType(MEDIA_TYPE_APPLICATION_JSON_UTF8).body(body);
    }

    /**
     * 取国际化翻译值或默认消息，取决于是否实际启用了国际化功能
     *
     * @param msgEnum 全局异常处理国际化消息枚举
     * @return 消息文本
     */
    protected String determineI18nMessage(@NonNull GlobalWebExceptionFriendlyMessageEnum msgEnum) {
//        if (Boolean.TRUE.equals(props.getI18n().getEnabled())) {
//            return I18nUtil.messageOf(msgEnum.i18nCode(), msgEnum.getDefaultValue());
//        }
        return msgEnum.getMessage();
    }
}
