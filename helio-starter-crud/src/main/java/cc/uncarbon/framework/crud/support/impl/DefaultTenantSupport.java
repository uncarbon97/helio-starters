package cc.uncarbon.framework.crud.support.impl;

import cc.uncarbon.framework.core.props.HelioProperties;
import cc.uncarbon.framework.crud.support.TenantSupport;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import lombok.extern.slf4j.Slf4j;

/**
 * 多租户支持-未启用
 */
@Slf4j
public class DefaultTenantSupport implements TenantSupport {

    @Override
    public void support(HelioProperties helioProperties, MybatisPlusInterceptor interceptor) {
        log.info("[多租户支持] >> 未启用");
    }
}
