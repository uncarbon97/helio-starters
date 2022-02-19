package cc.uncarbon.framework.tenant.resolver;


import cc.uncarbon.framework.core.props.HelioProperties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 请求头中租户信息解析器
 */
public interface TenantHeaderResolver {

    /**
     * 执行解析
     * @param request HttpServlet 请求
     * @param response HttpServlet 响应
     * @param handler
     * @param helioProperties
     */
    void resolve(HttpServletRequest request, HttpServletResponse response, Object handler, HelioProperties helioProperties);

}
