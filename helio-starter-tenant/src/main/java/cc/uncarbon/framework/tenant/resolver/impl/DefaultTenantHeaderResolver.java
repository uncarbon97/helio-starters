package cc.uncarbon.framework.tenant.resolver.impl;

import cc.uncarbon.framework.core.context.TenantContext;
import cc.uncarbon.framework.core.props.HelioProperties;
import cc.uncarbon.framework.tenant.resolver.TenantHeaderResolver;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DefaultTenantHeaderResolver implements TenantHeaderResolver {

    @Override
    public TenantContext resolve(HttpServletRequest request, HttpServletResponse response, Object handler, HelioProperties helioProperties) {
        return null;
    }
}
