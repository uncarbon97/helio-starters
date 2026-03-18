package cc.uncarbon.framework.helio.mybatis.autoconfigure;

import cc.uncarbon.framework.helio.base.enums.IdGeneratorStrategyEnum;
import cc.uncarbon.framework.helio.base.props.HelioProperties;
import cc.uncarbon.framework.helio.mybatis.handler.HelioSequenceIdGenerateHandler;
import cc.uncarbon.framework.helio.mybatis.handler.HelioSnowflakeIdGenerateHandler;
import cc.uncarbon.framework.helio.mybatis.handler.MybatisPlusAutoFillColumnHandler;
import cc.uncarbon.framework.helio.mybatis.support.TenantSupport;
import cc.uncarbon.framework.helio.mybatis.support.impl.DefaultTenantSupport;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequiredArgsConstructor
@AutoConfiguration
@Slf4j
public class HelioMybatisPlusAutoConfiguration {

    @Autowired(required = false)
    private TenantLineInnerInterceptor tenantLineInterceptor;

    private final HelioProperties helioProperties;


    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusInterceptor mybatisPlusInterceptor(
            TenantSupport tenantSupport
    ) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        /*
        https://baomidou.com/plugins/
        使用多个插件时，需要注意它们的顺序。建议的顺序是：
        多租户、动态表名
        分页、乐观锁
        SQL 性能规范、防止全表更新与删除
        总结：对 SQL 进行单次改造的插件应优先放入，不对 SQL 进行改造的插件最后放入。
         */
        if (Boolean.TRUE.equals(helioProperties.getTenant().getEnabled())) {
            if (tenantLineInterceptor != null) {
                interceptor.addInnerInterceptor(tenantLineInterceptor);
            }
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
