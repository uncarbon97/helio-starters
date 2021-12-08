package cc.uncarbon.framework.web.interceptor;

import cc.uncarbon.framework.core.context.TenantContext;
import cc.uncarbon.framework.core.context.UserContext;
import cc.uncarbon.framework.core.context.UserContextHolder;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 从请求头解析租户信息，并赋值到上下文
 *
 * @author Uncarbon
 */
@Slf4j
public class DefaultTenantParseInterceptor implements AsyncHandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (handler instanceof ResourceHttpRequestHandler) {
            // 直接放行静态资源
            return true;
        }

        /*
        只有未登录的情况下，才从请求头的 X-TenantId 和 X-TenantName 字段读取租户信息
        实际登录以后，用户上下文被更新，租户上下文随之固定

        注：这里是读取明文，而且未考虑负数、自然对数、科学计数法等情况；出于安全考虑可以合并成一个字段，并密文形式传输，这里编码解密；注意异常处理
         */
        if (!UserContextHolder.isActuallyHold()) {
            // 请求头里的租户ID，默认为-1
            String headerTenantIdStr = StrUtil.blankToDefault(request.getHeader("X-TenantId"), "-1");
            // 请求头里的租户名，可选
            String headerTenantName = request.getHeader("X-TenantName");
            if (StrUtil.isNumeric(headerTenantIdStr)) {
                Long headerTenantId = Long.parseLong(headerTenantIdStr);
                UserContextHolder.setUserContext(
                        UserContext.builder()
                                .relationalTenant(
                                        TenantContext.builder()
                                                .tenantId(headerTenantId)
                                                .tenantName(headerTenantName)
                                                .build()
                                )
                                .build()
                );
            }
        }
        return true;
    }
}
