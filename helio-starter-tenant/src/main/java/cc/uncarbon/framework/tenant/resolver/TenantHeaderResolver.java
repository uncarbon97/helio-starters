package cc.uncarbon.framework.tenant.resolver;


import cc.uncarbon.framework.core.context.TenantContext;
import cc.uncarbon.framework.core.props.HelioProperties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface TenantHeaderResolver {

    TenantContext resolve(HttpServletRequest request, HttpServletResponse response, Object handler, HelioProperties helioProperties);

}
