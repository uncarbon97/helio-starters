package cc.uncarbon.framework.tenant.support;

import cc.uncarbon.framework.core.props.HelioProperties;
import cc.uncarbon.framework.crud.support.TenantSupport;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import lombok.extern.slf4j.Slf4j;

/**
 * 多租户支持-数据源级
 */
@Slf4j
public class DataSourceTenantSupport implements TenantSupport {

    @Override
    public void support(HelioProperties helioProperties, MybatisPlusInterceptor interceptor) {
        throw new UnsupportedOperationException("暂不支持数据源级多租户");
    }
}
