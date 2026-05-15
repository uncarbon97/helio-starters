## 多语言解析优先级
```
1. QueryParamLangResolver  请求参数 ?lang=en_US（临时调试，不持久化）

2. HeaderLangResolver  从请求头获取
```

## 解析链
```
@Component
public class QueryParamLangResolver implements LangResolver {
    
    private final I18nProperties properties;
    
    @Override
    public Optional<Locale> resolve(HttpServletRequest request) {
        String paramName = properties.getResolver().getQueryParamName(); // 默认 "lang"
        String value = request.getParameter(paramName);
        return LocaleUtils.parseQuietly(value);
    }
    
    @Override
    public int getOrder() { return 10; }
}

@Component
public class HeaderLangResolver implements LangResolver {
    
    @Override
    public Optional<Locale> resolve(HttpServletRequest request) {
        // TODO 
    
        String header = request.getHeader("Accept-Language");
        if (StringUtils.isBlank(header)) return Optional.empty();

        // 按 q 值排序，找到第一个支持的
        return Locale.LanguageRange.parse(header).stream()
            .map(range -> Locale.forLanguageTag(range.getRange()))
            .filter(LocaleUtils::isSupported)
            .findFirst();
    }
    
    @Override
    public int getOrder() { return 40; }
}
```

## 组合解析器
```
@Component
public class CompositeLangResolver {
    
    private final List<LangResolver> resolvers;
    private final I18nProperties properties;
    
    public CompositeLangResolver(List<LangResolver> resolvers, I18nProperties properties) {
        this.resolvers = resolvers.stream()
            .sorted(Comparator.comparingInt(LangResolver::getOrder))
            .toList();
        this.properties = properties;
    }
    
    public Locale resolve(HttpServletRequest request) {
        for (LangResolver resolver : resolvers) {
            if (!isEnabled(resolver)) continue;
            try {
                Optional<Locale> locale = resolver.resolve(request);
                if (locale.isPresent() && LocaleUtils.isSupported(locale.get())) {
                    return locale.get();
                }
            } catch (Exception e) {
                log.warn("Locale resolver {} failed", resolver.getClass().getSimpleName(), e);
            }
        }
        return properties.getDefaultLocale();
    }
    
    private boolean isEnabled(LangResolver resolver) {
        return properties.getResolver().getEnabledResolvers()
            .contains(resolver.getClass().getSimpleName());
    }
}
```

## Servlet Filter - 入口拦截
```
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
public class LocaleContextFilter extends OncePerRequestFilter {
    
    private final CompositeLangResolver resolver;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain chain) throws IOException, ServletException {
        try {
            Locale locale = resolver.resolve(request);
            TimeZone timeZone = resolveTimeZone(request, locale);
            
            LocaleContext.set(locale, timeZone);
            // 同时设置 Spring 自己的 LocaleContextHolder，让 Spring 内置组件可用
            org.springframework.context.i18n.LocaleContextHolder.setLocale(locale);
            
            // 响应头告知客户端实际使用的 Locale
            response.setHeader("Content-Language", locale.toLanguageTag());
            
            chain.doFilter(request, response);
        } finally {
            LocaleContext.clear();
            org.springframework.context.i18n.LocaleContextHolder.resetLocaleContext();
        }
    }
    
    private TimeZone resolveTimeZone(HttpServletRequest request, Locale locale) {
        // 优先从 Header 取（前端可以传递 X-TimeZone: Asia/Shanghai）
        String tz = request.getHeader("X-TimeZone");
        if (StringUtils.isNotBlank(tz)) {
            try {
                return TimeZone.getTimeZone(ZoneId.of(tz));
            } catch (Exception ignored) {}
        }
        // 默认按 Locale 推断
        return TimeZone.getTimeZone(LocaleUtils.defaultZoneIdOf(locale));
    }
}
```