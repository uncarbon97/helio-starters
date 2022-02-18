package cc.uncarbon.framework.tenant.interceptor;


import cc.uncarbon.framework.tenant.resolver.TenantHeaderResolver;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 从请求头解析租户信息，并赋值到上下文
 *
 * @author Uncarbon
 */
@Slf4j
@RequiredArgsConstructor
public class TenantHeaderResolveInterceptor implements AsyncHandlerInterceptor {

    private final TenantHeaderResolver tenantHeaderResolver;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (handler instanceof ResourceHttpRequestHandler) {
            // 直接放行静态资源
            return true;
        }

        tenantHeaderResolver.resolve(request, response, handler);
        return true;
    }
}
