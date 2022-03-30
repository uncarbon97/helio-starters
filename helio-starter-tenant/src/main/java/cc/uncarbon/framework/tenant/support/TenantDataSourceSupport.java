package cc.uncarbon.framework.tenant.support;

import cc.uncarbon.framework.core.props.HelioProperties;
import cc.uncarbon.framework.crud.support.TenantSupport;
import cc.uncarbon.framework.tenant.config.GlobalTenantDataSourceConfiguration;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.GenericApplicationContext;

/**
 * 多租户支持-数据源级
 *
 * @author Uncarbon
 */
@Slf4j
@RequiredArgsConstructor
public class TenantDataSourceSupport implements TenantSupport {

    private final GenericApplicationContext applicationContext;

    @Override
    public void support(HelioProperties helioProperties, MybatisPlusInterceptor interceptor) {
        applicationContext.registerBean(GlobalTenantDataSourceConfiguration.class);

        log.info("\n\n[多租户支持] >> 隔离级别: 数据源级");

        System.err.println("数据源级多租户支持还处于试验阶段，请经过测试后再投入生产使用！");
    }
}
