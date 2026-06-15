package cc.uncarbon.framework.helium.web.handler;

import cc.uncarbon.framework.helium.base.errorcode.StructuredErrorCode;
import cc.uncarbon.framework.helium.base.errorcode.BuiltinErrorCodeEnum;
import cc.uncarbon.framework.helium.base.exception.BusinessException;
import cc.uncarbon.framework.helium.i18n.props.HeliumI18nProperties;
import cc.uncarbon.framework.helium.i18n.util.I18nMessageUtil;
import cc.uncarbon.framework.helium.web.model.response.ApiResult;
import cc.uncarbon.framework.helium.web.util.InvalidFieldUtil;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.hutool.core.text.CharSequenceUtil;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
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

    private final ObjectProvider<HeliumI18nProperties> i18nPropsProvider;
    private static boolean I18N_ENABLED = false;

    @PostConstruct
    public void postConstruct() {
        HeliumI18nProperties props = i18nPropsProvider.getIfAvailable();
        if (props != null) {
            I18N_ENABLED = props.getEnabled();
        }
    }

    /**
     * 主动抛出的业务异常
     */
    @ExceptionHandler(value = {BusinessException.class})
    public ResponseEntity<ApiResult<Void>> handleBusinessException(BusinessException e, HttpServletRequest servletRequest) {
        this.logException(e, servletRequest);

        ApiResult<Void> ret;
        if (e.getErrorCode() != null) {
            ret = ApiResult.fail(e.getCode(), determineI18nMessage(e.getErrorCode()));
        } else {
            ret = ApiResult.fail(e.getCode(), e.getMessage());
        }
        return createResponseEntity(HttpStatus.BAD_REQUEST, ret);
    }

    /**
     * 用户登录异常
     */
    @ExceptionHandler(value = {NotLoginException.class})
    public ResponseEntity<ApiResult<Void>> handleNotLoginException(NotLoginException e, HttpServletRequest servletRequest) {
        this.logException(e, servletRequest);

        final var codeEnum = BuiltinErrorCodeEnum.Z00401;
        ApiResult<Void> ret = ApiResult.fail(codeEnum.getErrorCode(), determineI18nMessage(codeEnum));
        return createResponseEntity(HttpStatus.UNAUTHORIZED, ret);
    }

    /**
     * 用户角色异常
     */
    @ExceptionHandler(value = {NotRoleException.class})
    public ResponseEntity<ApiResult<Void>> handleNotRoleException(NotRoleException e, HttpServletRequest servletRequest) {
        this.logException(e, servletRequest);

        final var codeEnum = BuiltinErrorCodeEnum.Z00402;
        ApiResult<Void> ret = ApiResult.fail(codeEnum.getErrorCode(), determineI18nMessage(codeEnum));
        return createResponseEntity(HttpStatus.FORBIDDEN, ret);
    }

    /**
     * 用户权限异常
     */
    @ExceptionHandler(value = {NotPermissionException.class})
    public ResponseEntity<ApiResult<Void>> handleNotPermissionException(NotPermissionException e,
                                                                        HttpServletRequest servletRequest) {
        this.logException(e, servletRequest);

        final var codeEnum = BuiltinErrorCodeEnum.Z00403;
        ApiResult<Void> ret = ApiResult.fail(codeEnum.getErrorCode(), determineI18nMessage(codeEnum));
        return createResponseEntity(HttpStatus.FORBIDDEN, ret);
    }

    /**
     * 404
     */
    @ExceptionHandler(value = {NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ApiResult<Void>> handleNoHandlerFoundException(Exception e, HttpServletRequest servletRequest) {
        this.logException(e, servletRequest);

        final var codeEnum = BuiltinErrorCodeEnum.Z00404;
        ApiResult<Void> ret = ApiResult.fail(codeEnum.getErrorCode(), determineI18nMessage(codeEnum));
        return createResponseEntity(HttpStatus.NOT_FOUND, ret);
    }

    /**
     * 请求方式不对
     * HttpRequestMethodNotSupportedException 如：POST接口用了GET请求
     * HttpMediaTypeNotSupportedException 如：Content-type 应为 application/json 的，使用了 text/plain
     */
    @ExceptionHandler(value = {HttpRequestMethodNotSupportedException.class, HttpMediaTypeNotSupportedException.class})
    public ResponseEntity<ApiResult<Void>> handleServletException(ServletException e,
                                                                  HttpServletRequest servletRequest) {
        this.logException(e, servletRequest);

        final var codeEnum = BuiltinErrorCodeEnum.Z00405;
        ApiResult<Void> ret = ApiResult.fail(codeEnum.getErrorCode(), determineI18nMessage(codeEnum));
        return createResponseEntity(HttpStatus.METHOD_NOT_ALLOWED, ret);
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
    @ExceptionHandler(value = {JsonParseException.class, HttpMessageNotReadableException.class,
            IllegalArgumentException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiResult<Void>> handleJsonParseException(Exception e, HttpServletRequest servletRequest) {
        this.logException(e, servletRequest);

        final var codeEnum = BuiltinErrorCodeEnum.Z00406;
        ApiResult<Void> ret = ApiResult.fail(codeEnum.getErrorCode(), determineI18nMessage(codeEnum));
        return createResponseEntity(HttpStatus.NOT_ACCEPTABLE, ret);
    }

    /**
     * JSR303 表单参数校验失败，或入参格式转换失败
     * 需在 Controller 层使用@Valid注解
     */
    @ExceptionHandler(value = {MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ApiResult<InvalidFieldUtil.InvalidField>> handleBindException(BindException e,
                                                                                        HttpServletRequest servletRequest) {
        this.logException(e, servletRequest);

        final var codeEnum = BuiltinErrorCodeEnum.Z00406;
        ApiResult<InvalidFieldUtil.InvalidField> ret = ApiResult.fail(
                codeEnum.getErrorCode(), determineI18nMessage(codeEnum),
                InvalidFieldUtil.getInvalidField(e.getBindingResult()));
        return createResponseEntity(HttpStatus.NOT_ACCEPTABLE, ret);
    }

    /**
     * 兜底未归类异常，如：
     * ClientException(Dubbo RPC)
     * RpcException(Dubbo RPC)
     * SQLException
     * RuntimeException
     * 一类的都会落到这里来，并打印堆栈
     */
    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<ApiResult<Void>> handleException(Exception e, HttpServletRequest servletRequest) {
        // 打印堆栈，方便溯源
        this.logException(e, servletRequest, true);

        final var codeEnum = BuiltinErrorCodeEnum.Z00500;
        ApiResult<Void> ret = ApiResult.fail(codeEnum.getErrorCode(), determineI18nMessage(codeEnum));
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
     * @param errorCode 错误码
     * @return 消息文本
     */
    protected String determineI18nMessage(@NonNull StructuredErrorCode errorCode, Object... templateParams) {
        if (I18N_ENABLED) {
            return I18nMessageUtil.messageOf(errorCode.getErrorCode(), errorCode.getErrorMsgFriendly(), templateParams);
        }
        return CharSequenceUtil.format(errorCode.getErrorMsgFriendly(), templateParams);
    }
}
