package cc.uncarbon.tenant.resolver;


import cc.uncarbon.framework.core.context.TenantContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface TenantHeaderResolver {

    TenantContext resolve(HttpServletRequest request, HttpServletResponse response, Object handler);

}
