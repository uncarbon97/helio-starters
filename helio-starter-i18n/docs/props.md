```yaml
helio.i18n:
  enabled: true
  # 多语言
  lang:
    default-locale: zh_CN
    # 只加载支持的语言文件
    supported-locales:
      - zh_CN
      - en_US
    cache-duration: 0 # 开发环境设置为0，实现热重载
    basenames:
      - classpath*:i18n/messages
      - classpath*:i18n/errors
      - classpath*:i18n/validation
      # 业务模块，如 sys、order 等
      - classpath*:i18n/modules
    resolver:
      # 用于 QueryParamLocaleResolver，从 URL 请求参数中读取语言，如「?lang=en_US」
      query-param-name: lang
      # 用于 HeaderLocaleResolver，从 HTTP 请求头中中读取语言，如「X-i18n-Lang=en_US」
      header-name: "X-i18n-Lang"
  timezone:
    # IANA 标准：Asia/Shanghai、America/New_York、Europe/London
    resolver:
      # 用于 QueryParamTimezoneResolver，从 URL 请求参数中读取时区，如「?lang=en_US」
      query-param-name: lang
      # 用于 HeaderLocaleResolver，从 HTTP 请求头中中读取语言，如「X-i18n-Lang=en_US」
      header-name: "X-i18n-Lang"
```