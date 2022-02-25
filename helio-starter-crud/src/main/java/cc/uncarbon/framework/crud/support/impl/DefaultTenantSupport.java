package cc.uncarbon.framework.crud.support.impl;

import cc.uncarbon.framework.core.props.HelioProperties;
import cc.uncarbon.framework.crud.support.TenantSupport;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;

/**
 * 多租户支持-默认处理
 */
public class DefaultTenantSupport implements TenantSupport {

    @Override
    public void support(HelioProperties helioProperties, MybatisPlusInterceptor interceptor) {
        System.err.println("\n[多租户支持] >> 您启用了多租户，但未引入 helio-starter-tenant，无法对 SQL 进行拦截处理\n");
    }
}
