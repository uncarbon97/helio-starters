package cc.uncarbon.framework.helium.websecurity.xss;

import cn.hutool.core.collection.CollUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.util.UrlPathHelper;

import java.io.IOException;
import java.util.List;

/**
 * XSS过滤器
 *
 * @author Mark@renren-io
 * @author Uncarbon
 */
@Slf4j
public class XssFilter implements Filter {

	/**
	 * 不进行过滤的路由（直接放行）
	 */
	private final List<String> ignoredUrls;

	/**
	 * JSON body 缓冲上限（字节），见 {@link XssHttpServletRequestWrapper#DEFAULT_MAX_BODY_BYTES}
	 */
	private final long maxBodyBytes;

	private static final AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();

	/**
	 * 解码 + 归一化（去 contextPath、折叠 "//"、解析 "." / ".."、百分号解码）后再做 Ant 匹配，
	 * 避免 {@code /api/%61dmin}、{@code //api/admin}、{@code /api/./admin} 与 {@code /api/admin} 匹配结果不一致
	 */
	private static final UrlPathHelper URL_PATH_HELPER;

	static {
		UrlPathHelper helper = new UrlPathHelper();
		helper.setUrlDecode(true);
		helper.setRemoveSemicolonContent(true);
		helper.setAlwaysUseFullPath(true);
		URL_PATH_HELPER = helper;
	}

	public XssFilter(List<String> ignoredUrls) {
		this(ignoredUrls, XssHttpServletRequestWrapper.DEFAULT_MAX_BODY_BYTES);
	}

	public XssFilter(List<String> ignoredUrls, long maxBodyBytes) {
		this.ignoredUrls = ignoredUrls;
		this.maxBodyBytes = maxBodyBytes;
	}


	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		if (canExclude(httpServletRequest)) {
			// 不经过XSS过滤，交给下一个过滤器
			chain.doFilter(httpServletRequest, response);
			return;
		}

		XssHttpServletRequestWrapper xssRequest = new XssHttpServletRequestWrapper(httpServletRequest, maxBodyBytes);
		try {
			chain.doFilter(xssRequest, response);
		} catch (RequestBodyTooLargeException e) {
			// 包装器在下游首次读 body 时才会触发；此处统一响应 413
			if (response instanceof HttpServletResponse httpResponse && !httpResponse.isCommitted()) {
				log.warn("[XssFilter] 请求体超过 XSS 缓冲上限，已拒绝 >> URI={}, max={}B", httpServletRequest.getRequestURI(), e.getMaxBytes());
				httpResponse.resetBuffer();
				httpResponse.sendError(HttpStatus.PAYLOAD_TOO_LARGE.value(), "Request body too large");
				return;
			}
			throw e;
		}
	}

	/**
	 * 该路由是否可以排除XSS过滤
	 */
	private boolean canExclude(HttpServletRequest request) {
		if (CollUtil.isEmpty(ignoredUrls)) {
			return false;
		}

		/*
		用解码并归一化后的「应用内路径」匹配：
		- Boot 4 DispatcherServlet 映射 /*，getServletPath() 恒返回空串，不能用它
		- getRequestURI() 未解码、未归一化，直接匹配可被编码/重复斜杠绕过
		 */
		String lookupPath = URL_PATH_HELPER.getPathWithinApplication(request);
		String normalized = normalize(lookupPath);
		return ignoredUrls.stream().anyMatch(
				excludedRoute -> ANT_PATH_MATCHER.match(excludedRoute, normalized)
		);
	}

	/**
	 * 折叠重复斜杠并解析 "." / ".." 段；越过根的 ".." 直接丢弃
	 */
	static String normalize(String path) {
		if (path == null || path.isEmpty()) {
			return "/";
		}
		String[] segments = path.split("/");
		java.util.ArrayDeque<String> stack = new java.util.ArrayDeque<>();
		for (String segment : segments) {
			if (segment.isEmpty() || ".".equals(segment)) {
				continue;
			}
			if ("..".equals(segment)) {
				stack.pollLast();
				continue;
			}
			stack.addLast(segment);
		}
		if (stack.isEmpty()) {
			return "/";
		}
		StringBuilder sb = new StringBuilder();
		for (String segment : stack) {
			sb.append('/').append(segment);
		}
		return sb.toString();
	}
}
