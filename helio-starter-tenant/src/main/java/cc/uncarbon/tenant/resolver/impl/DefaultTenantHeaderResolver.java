package cc.uncarbon.tenant.resolver.impl;

import cc.uncarbon.framework.core.context.TenantContext;
import cc.uncarbon.tenant.resolver.TenantHeaderResolver;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DefaultTenantHeaderResolver implements TenantHeaderResolver {

    @Override
    public TenantContext resolve(HttpServletRequest request, HttpServletResponse response, Object handler) {
        return null;
    }
}
