/*
 * Copyright © 2017-2023 Knife4j(xiaoymin@foxmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package cc.uncarbon.framework.knife4j.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;

import java.io.IOException;

/**
 * @author <a href="xiaoymin@foxmail.com">xiaoymin@foxmail.com</a>
 * 2023/2/25 19:17
 * @since knife4j
 */
@Data
public class JakartaServletSecurityBasicAuthFilter extends AbstractSecurityFilter implements Filter {

    /***
     * basic auth验证
     */
    public static final String KNIFE4J_BASIC_AUTH_SESSION = "KNIFE4J_BASIC_AUTH_SESSION";

    /**
     * 校验Basic请求头
     */
    public static final String AUTH_HEADER_NAME = "Authorization";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.initServletConfig(filterConfig.getInitParameterNames(),
                filterConfig.getInitParameter("enableBasicAuth"),
                filterConfig.getInitParameter("userName"),
                filterConfig.getInitParameter("password"));
    }
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String url = request.getRequestURI();
        if (this.isEnableBasicAuth() && this.match(url)) {
            Object sessionObject = request.getSession().getAttribute(KNIFE4J_BASIC_AUTH_SESSION);
            String auth = request.getHeader(AUTH_HEADER_NAME);
            if (this.tryCommonBasic(url, sessionObject, auth)) {
                if (sessionObject == null) {
                    request.getSession().setAttribute(KNIFE4J_BASIC_AUTH_SESSION, getUserName());
                }
                filterChain.doFilter(servletRequest, servletResponse);
            } else {
                writeJakartaForbiddenCode(response);
            }
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    @Override
    public void destroy() {
        this.urlFilters = null;
    }

    /**
     * 响应Basic
     */
    private static void writeJakartaForbiddenCode(jakarta.servlet.http.HttpServletResponse httpServletResponse) throws IOException {
        httpServletResponse.setStatus(401);
        httpServletResponse.setHeader("WWW-Authenticate", "Basic realm=\"input Swagger Basic userName & password \"");
        httpServletResponse.getWriter().write("You do not have permission to access this resource");
    }
}
