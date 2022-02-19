package cc.uncarbon.framework.tenant.resolver.impl;

import cc.uncarbon.framework.core.props.HelioProperties;
import cc.uncarbon.framework.tenant.resolver.TenantHeaderResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DefaultTenantHeaderResolver implements TenantHeaderResolver {

    @Override
    public void resolve(HttpServletRequest request, HttpServletResponse response, Object handler, HelioProperties helioProperties) {
        // 什么也不做，也不改变当前租户上下文
    }
}
