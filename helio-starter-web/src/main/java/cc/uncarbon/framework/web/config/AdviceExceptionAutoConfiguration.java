package cc.uncarbon.framework.web.config;

import cc.uncarbon.framework.core.constant.HelioConstant;
import cc.uncarbon.framework.core.exception.BusinessException;
import cc.uncarbon.framework.web.model.response.ApiResult;
import cc.uncarbon.framework.web.util.InvalidFieldUtil;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.fasterxml.jackson.core.JsonParseException;
import java.nio.charset.StandardCharsets;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.core.annotation.Order;
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

/**
 * Web 全局异常处理自动配置类
 *
 * @author Uncarbon
 */
@Slf4j
@RestController
@ControllerAdvice
@AutoConfiguration
// 主动标注为最低优先级，便于覆盖
@Order
public class AdviceExceptionAutoConfiguration {

    protected static final MediaType MEDIA_TYPE = new MediaType("application", "json", StandardCharsets.UTF_8);
    protected static final String DUBBO_PACKAGE_PREFIX = "org.apache.dubbo";


    /**
     * 主动抛出的业务异常
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({BusinessException.class})
    public ResponseEntity<ApiResult<?>> handleBusinessException(BusinessException e, HttpServletRequest request, HttpServletResponse response) {
        this.logError(e, request);
        ApiResult<?> ret = ApiResult.fail(e.getCode(), e.getMessage());
        return createResponseEntity(HttpStatus.BAD_REQUEST, ret);
    }

    /**
     * 用户登录异常
     */
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler({NotLoginException.class})
    public ResponseEntity<ApiResult<?>> handleNotLoginException(NotLoginException e, HttpServletRequest request) {
        this.logError(e, request);
        ApiResult<?> ret = ApiResult.fail(HttpStatus.UNAUTHORIZED.value(), "请您先登录");
        return createResponseEntity(HttpStatus.UNAUTHORIZED, ret);
    }

    /**
     * 用户权限异常
     */
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler({NotPermissionException.class})
    public ResponseEntity<ApiResult<?>> handleNotPermissionException(NotPermissionException e, HttpServletRequest request) {
        this.logError(e, request);
        ApiResult<?> ret = ApiResult.fail(HttpStatus.FORBIDDEN.value(), "您权限不足");
        return createResponseEntity(HttpStatus.FORBIDDEN, ret);
    }

    /**
     * 用户身份异常
     */
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler({NotRoleException.class})
    public ResponseEntity<ApiResult<?>> handleNotRoleException(NotRoleException e, HttpServletRequest request) {
        this.logError(e, request);
        ApiResult<?> ret = ApiResult.fail(HttpStatus.FORBIDDEN.value(), "您与要求角色不符");
        return createResponseEntity(HttpStatus.FORBIDDEN, ret);
    }

    /**
     * 404 NOT FOUND
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({NoHandlerFoundException.class})
    public ResponseEntity<ApiResult<?>> handleNoHandlerFoundException(NoHandlerFoundException e, HttpServletRequest request) {
        this.logError(e, request);
        ApiResult<?> ret = ApiResult.fail(HttpStatus.NOT_FOUND.value(), "你迷路啦");
        return createResponseEntity(HttpStatus.NOT_FOUND, ret);
    }

    /**
     * JsonParseException, HttpMessageNotReadableException
     * Jackson反序列化异常
     * 通常是因为JSON格式错误，或枚举输入值超出范围
     *
     * IllegalArgumentException
     * 不合法的参数异常
     *
     * MethodArgumentTypeMismatchException
     * "@PathVariable" 注解收参类型为 Long，但传的是 String
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({JsonParseException.class, HttpMessageNotReadableException.class, IllegalArgumentException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiResult<?>> handleJsonParseException(Exception e, HttpServletRequest request) {
        this.logError(e, request);
        ApiResult<?> ret = ApiResult.fail(HttpStatus.NOT_ACCEPTABLE.value(), "错误参数格式或值");
        return createResponseEntity(HttpStatus.BAD_REQUEST, ret);
    }

    /**
     * JSR303 表单参数校验失败，或入参格式转换失败
     * 需在 Controller 层使用@Valid注解
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ApiResult<?>> handleBindException(BindException e, HttpServletRequest request) {
        this.logError(e, request);
        ApiResult<?> ret = ApiResult.fail(HttpStatus.NOT_ACCEPTABLE.value(), "错误参数格式或值", InvalidFieldUtil.getInvalidField(e.getBindingResult()));
        return createResponseEntity(HttpStatus.BAD_REQUEST, ret);
    }

    /**
     * 请求方式不对, 比如POST接口用了GET请求
     */
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler({HttpRequestMethodNotSupportedException.class})
    public ResponseEntity<ApiResult<?>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        this.logError(e, request);
        ApiResult<?> ret = ApiResult.fail(HttpStatus.METHOD_NOT_ALLOWED.value(), "错误的请求方式");
        return createResponseEntity(HttpStatus.METHOD_NOT_ALLOWED, ret);
    }

    /**
     * 兜底未归类异常
     * 如ClientException(Dubbo RPC), RpcException(Dubbo RPC), SQLException, RuntimeException一类的都会掉到这里来, 并打印堆栈
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({Exception.class})
    public ResponseEntity<ApiResult<?>> handleException(Exception e, HttpServletRequest request) {
        this.logError(e, request);
        // 打印堆栈，方便溯源
        e.printStackTrace();

        int responseCode = HttpStatus.INTERNAL_SERVER_ERROR.value();

        if (e.getClass().getName().startsWith(DUBBO_PACKAGE_PREFIX)) {
            // Dubbo RPC异常
            responseCode = HelioConstant.Dubbo.RPC_EXCEPTION_RESPONSE_CODE;
        }

        ApiResult<?> ret = ApiResult.fail(responseCode, "请稍后再试");
        return createResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, ret);
    }

    protected void logError(Exception e, HttpServletRequest request) {
        log.error("[Web][有异常被抛出] >> 异常类=[{}], URI=[{}], 消息=[{}]", e.getClass().getName(), request.getRequestURI(), e.getMessage());
    }

    protected static ResponseEntity<ApiResult<?>> createResponseEntity(HttpStatus httpStatus, ApiResult<?> body) {
        return ResponseEntity.status(httpStatus.value()).contentType(MEDIA_TYPE).body(body);
    }
}
