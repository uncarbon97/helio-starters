package cc.uncarbon.framework.crud.config;

import cc.uncarbon.framework.core.enums.TenantIsolateLevelEnum;
import cc.uncarbon.framework.core.props.HelioProperties;
import cc.uncarbon.framework.crud.handler.HelioIdentifierGeneratorHandler;
import cc.uncarbon.framework.crud.handler.HelioTenantLineHandler;
import cc.uncarbon.framework.crud.handler.MybatisPlusAutoFillColumnHandler;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.Resource;

/**
 * Mybatis-Plus配置类
 * @author Uncarbon
 */
@EnableTransactionManagement(
        proxyTargetClass = true
)
@Configuration
public class HelioMybatisPlusAutoConfiguration {

    @Resource
    private HelioProperties helioProperties;

    @Resource
    private HelioTenantLineHandler helioTenantLineHandler;


    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        /*
        https://mybatis.plus/guide/interceptor.html#mybatisplusinterceptor
        使用多个功能需要注意顺序关系,建议使用如下顺序

        多租户,动态表名
        分页,乐观锁
        sql性能规范,防止全表更新与删除
         */
        // 多租户 && 行级租户隔离级别
        if (Boolean.TRUE.equals(helioProperties.getCrud().getTenant().getEnabled())
                && TenantIsolateLevelEnum.LINE.equals(helioProperties.getCrud().getTenant().getIsolateLevel())
        ) {
            interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(helioTenantLineHandler));
        }

        // 分页插件
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.getDbType(helioProperties.getCrud().getDbType()));
        // 设置sql的limit为无限制，默认是500
        paginationInnerInterceptor.setMaxLimit(-1L);
        interceptor.addInnerInterceptor(paginationInnerInterceptor);

        // 乐观锁
        if (Boolean.TRUE.equals(helioProperties.getCrud().getOptimisticLock().getEnabled())) {
            interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        }

        // 防止全表更新与删除
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());


        return interceptor;
    }

    /**
     * 自定义ID生成器
     */
    @Bean
    public IdentifierGenerator helioSnowflakeIdentifierGeneratorHandler() {
        return new HelioIdentifierGeneratorHandler(helioProperties);
    }

    /**
     * 字段自动填充
     */
    @Bean
    public MybatisPlusAutoFillColumnHandler mybatisPlusAutoFillColumnHandler() {
        return new MybatisPlusAutoFillColumnHandler();
    }

    /**
     * 行级租户
     */
    @Bean
    public HelioTenantLineHandler helioTenantLineHandler() {
        return new HelioTenantLineHandler();
    }

}
