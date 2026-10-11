package cc.uncarbon.framework.helium.websecurity.xss;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.util.List;

/**
 * XSS过滤器
 *
 * @author Mark@renren-io
 */
@RequiredArgsConstructor
public class XssFilter implements Filter {

	/**
	 * 不进行过滤的路由（直接放行）
	 */
	private final List<String> ignoredUrls;

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
		if (CollUtil.isEmpty(ignoredUrls)) {
			return false;
		}

		/*
		用 requestURI 去掉 contextPath 后匹配：
		Boot 4 DispatcherServlet 映射 /*，getServletPath() 恒返回空串，导致排除路径永不命中
		 */
		String requestUri = request.getRequestURI();
		String contextPath = request.getContextPath();
		if (CharSequenceUtil.isNotEmpty(contextPath) && requestUri.startsWith(contextPath)) {
			requestUri = requestUri.substring(contextPath.length());
		}
		String finalRequestUri = requestUri;
		return ignoredUrls.stream().anyMatch(
				excludedRoute -> ANT_PATH_MATCHER.match(excludedRoute, finalRequestUri)
		);
	}
}
