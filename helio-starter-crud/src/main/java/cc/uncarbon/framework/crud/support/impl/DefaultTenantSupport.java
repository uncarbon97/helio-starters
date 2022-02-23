package cc.uncarbon.framework.crud.support.impl;

import cc.uncarbon.framework.core.props.HelioProperties;
import cc.uncarbon.framework.crud.support.TenantSupport;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import lombok.extern.slf4j.Slf4j;

/**
 * 多租户支持-默认处理
 */
@Slf4j
public class DefaultTenantSupport implements TenantSupport {

    @Override
    public void support(HelioProperties helioProperties, MybatisPlusInterceptor interceptor) {
        log.error("[多租户支持] >> 您启用了多租户，但未引入 helio-starter-tenant，无法对 SQL 进行拦截处理");
    }
}
