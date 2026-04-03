package cc.uncarbon.framework.helio.mybatis.strategy;

import cc.uncarbon.framework.helio.base.autoconfigure.HelioProperties;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;

/**
 * Mybatis-Plus 多租户支持接口
 *
 * @author Uncarbon
 */
public interface TenantSupport {

    /**
     * 不同的多租户隔离级别分别实现本方法，按需添加 SQL 拦截器
     * @param helioProperties Helio 配置属性
     * @param interceptor Mybatis-Plus 拦截器
     */
    void support(HelioProperties helioProperties, MybatisPlusInterceptor interceptor);

}
