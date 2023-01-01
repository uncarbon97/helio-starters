package cc.uncarbon.framework.crud.config;

import cc.uncarbon.framework.core.enums.IdGeneratorStrategyEnum;
import cc.uncarbon.framework.core.props.HelioProperties;
import cc.uncarbon.framework.crud.handler.HelioSequenceIdGenerateHandler;
import cc.uncarbon.framework.crud.handler.HelioSnowflakeIdGenerateHandler;
import cc.uncarbon.framework.crud.handler.MybatisPlusAutoFillColumnHandler;
import cc.uncarbon.framework.crud.support.TenantSupport;
import cc.uncarbon.framework.crud.support.impl.DefaultTenantSupport;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Helio Mybatis-Plus 自动配置类
 *
 * @author Uncarbon
 */
@EnableTransactionManagement(
        proxyTargetClass = true
)
@AutoConfiguration
@Slf4j
@RequiredArgsConstructor
public class HelioMybatisPlusAutoConfiguration {

    private final HelioProperties helioProperties;


    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusInterceptor mybatisPlusInterceptor(
            TenantSupport tenantSupport
    ) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        /*
        https://baomidou.com/pages/2976a3/#%E5%B1%9E%E6%80%A7
        使用多个功能需要注意顺序关系,建议使用如下顺序

        多租户,动态表名
        分页,乐观锁
        sql性能规范,防止全表更新与删除
         */
        if (Boolean.TRUE.equals(helioProperties.getTenant().getEnabled())) {
            // 配置文件中启用了多租户功能，注入对应支持 bean
            tenantSupport.support(helioProperties, interceptor);
        }

        /*
        分页插件
         */
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
        // 设置sql的limit为无限制
        paginationInnerInterceptor.setMaxLimit(-1L);
        interceptor.addInnerInterceptor(paginationInnerInterceptor);

        /*
        乐观锁
         */
        if (Boolean.TRUE.equals(helioProperties.getCrud().getOptimisticLock().getEnabled())) {
            interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        }

        /*
        防止全表更新与删除
         */
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());

        return interceptor;
    }

    /**
     * 自定义ID生成器 - 雪花ID
     */
    @Bean
    @ConditionalOnMissingBean
    public IdentifierGenerator helioIdentifierGenerator() {
        IdGeneratorStrategyEnum strategy = helioProperties.getCrud().getIdGenerator().getStrategy();

        if (strategy == IdGeneratorStrategyEnum.SNOWFLAKE) {
            return new HelioSnowflakeIdGenerateHandler(helioProperties);
        }

        if (strategy == IdGeneratorStrategyEnum.SEQUENCE) {
            return new HelioSequenceIdGenerateHandler();
        }

        throw new IllegalArgumentException("Value of 'helio.crud.idGenerator.strategy' is illegal");
    }

    /**
     * 字段自动填充
     */
    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusAutoFillColumnHandler mybatisPlusAutoFillColumnHandler() {
        return new MybatisPlusAutoFillColumnHandler();
    }

    /**
     * 默认租户支持类
     */
    @Bean
    @ConditionalOnMissingBean
    public TenantSupport defaultTenantSupport() {
        return new DefaultTenantSupport();
    }

}
