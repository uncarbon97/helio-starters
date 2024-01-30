package cc.uncarbon.framework.dubbo.filter;

import cc.uncarbon.framework.core.exception.BusinessException;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.utils.ReflectUtils;
import org.apache.dubbo.rpc.*;
import org.apache.dubbo.rpc.filter.ExceptionFilter;
import org.apache.dubbo.rpc.service.GenericService;

import java.lang.reflect.Method;

/**
 * 自定义Dubbo异常处理
 *
 * @author Zhu JW
 * @author Uncarbon
 **/
@Slf4j
public class HelioDubboExceptionFilter extends ExceptionFilter {

    @Override
    public void onResponse(Result appResponse, Invoker<?> invoker, Invocation invocation) {
        if (!(appResponse.hasException() && GenericService.class != invoker.getInterface())) {
            return;
        }

        try {
            Throwable exception = appResponse.getException();
            if (isExceptionCheckedOrAppearsInSign(invoker, invocation, exception)) {
                return;
            }

            // for the exception not found in method's signature, print ERROR message in server's log.
            log.error(
                    "Got unchecked and undeclared exception which called by {}. service: {}, method: {}, exception: {} >> {}",
                    RpcContext.getServiceContext().getRemoteAddress(), invoker.getInterface().getName(),
                    invocation.getMethodName(), exception.getClass().getName(), exception.getMessage(), exception);

            if (canReturnDirectly(invoker, exception)) {
                return;
            }

            // otherwise, wrap with RuntimeException and throw back to the client
            appResponse.setException(new RuntimeException(StrUtil.toString(exception)));
        } catch (NoSuchMethodException cause) {
            // ignored
        } catch (Throwable cause) {
            log.warn("Fail to ExceptionFilter when called by {}. service: {}, method: {}, exception: {} >> {}",
                    RpcContext.getServiceContext().getRemoteHost(), invoker.getInterface().getName(),
                    invocation.getMethodName(), cause.getClass().getName(), cause.getMessage(), cause);
        }
    }

    @Override
    public void onError(Throwable e, Invoker<?> invoker, Invocation invocation) {
        log.error(
                "Got unchecked and undeclared exception which called by {}. service: {}, method: {}, exception: {} >> {}",
                RpcContext.getServiceContext().getRemoteHost(), invoker.getInterface().getName(),
                invocation.getMethodName(), e.getClass().getName(), e.getMessage(), e);
    }

    /*
    ----------------------------------------------------------------
                        私有方法 private methods
    ----------------------------------------------------------------
     */

    private static boolean isExceptionCheckedOrAppearsInSign(Invoker<?> invoker, Invocation invocation,
                                                             Throwable exception) throws NoSuchMethodException {
        // directly throw if it's checked exception
        if (!(exception instanceof RuntimeException) && (exception instanceof Exception)) {
            return true;
        }

        // directly throw if the exception appears in the signature
        Method method = invoker.getInterface().getMethod(invocation.getMethodName(), invocation.getParameterTypes());
        Class<?>[] exceptionClasses = method.getExceptionTypes();
        for (Class<?> exceptionClass : exceptionClasses) {
            if (exception.getClass().equals(exceptionClass)) {
                return true;
            }
        }
        return false;
    }

    private static boolean canReturnDirectly(Invoker<?> invoker, Throwable exception) {
        // directly throw if exception class and interface class are in the same jar file.
        String serviceFile = ReflectUtils.getCodeBase(invoker.getInterface());
        String exceptionFile = ReflectUtils.getCodeBase(exception.getClass());
        if (serviceFile == null || exceptionFile == null || serviceFile.equals(exceptionFile)) {
            return true;
        }

        // directly throw if it's JDK exception
        String className = exception.getClass().getName();
        if (CharSequenceUtil.startWithAny(className, "java.", "javax.", "jakarta.")) {
            return true;
        }
        // directly throw if it's dubbo exception
        if (exception instanceof RpcException) {
            return true;
        }
        // directly throw if it's BusinessException
        return exception instanceof BusinessException;
    }

}
