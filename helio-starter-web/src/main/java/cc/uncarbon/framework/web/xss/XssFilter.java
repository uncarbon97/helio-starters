package cc.uncarbon.framework.web.xss;

import cn.hutool.core.collection.CollUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.util.List;

/**
 * XSS过滤器
 *
 * @author Mark sunlightcs@gmail.com
 */
@RequiredArgsConstructor
public class XssFilter implements Filter {

	/**
	 * 不进行过滤的路由（直接放行）
	 */
	private final List<String> excludedRoutes;

	private static final AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();


	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		if (canExclude(httpServletRequest)) {
			// 不经过XSS过滤，交给下一个过滤器
			chain.doFilter(httpServletRequest, response);
			return;
		}

		XssHttpServletRequestWrapper xssRequest = new XssHttpServletRequestWrapper(httpServletRequest);
		chain.doFilter(xssRequest, response);
	}

	/**
	 * 该路由是否可以排除XSS过滤
	 */
	private boolean canExclude(HttpServletRequest request) {
		if (CollUtil.isEmpty(excludedRoutes)) {
			return false;
		}

		String requestUri = request.getServletPath();
		return excludedRoutes.stream().anyMatch(
				excludedRoute -> ANT_PATH_MATCHER.match(excludedRoute, requestUri)
		);
	}
}
