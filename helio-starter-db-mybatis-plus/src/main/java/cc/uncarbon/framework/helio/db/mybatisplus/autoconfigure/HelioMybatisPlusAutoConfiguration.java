package cc.uncarbon.framework.helio.db.mybatisplus.autoconfigure;

import cc.uncarbon.framework.helio.db.mybatisplus.idgen.HutoolSnowflakeIdGenerator;
import cc.uncarbon.framework.helio.db.mybatisplus.handler.MybatisPlusAutoFillColumnHandler;
import cc.uncarbon.framework.helio.db.mybatisplus.props.HelioIdGenProperties;
import cc.uncarbon.framework.helio.db.mybatisplus.props.HelioMybatisPlusProperties;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Helio Mybatis-Plus 自动配置类
 *
 * @author Uncarbon
 */
@EnableConfigurationProperties(value = {HelioMybatisPlusProperties.class, HelioIdGenProperties.class})
@EnableTransactionManagement(
        proxyTargetClass = true
)
@RequiredArgsConstructor
@AutoConfiguration
@Slf4j
public class HelioMybatisPlusAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusInterceptor mybatisPlusInterceptor(
            HelioMybatisPlusProperties props,
            // 所有注入到 Spring 容器中的 InnerInterceptor，其中包括行级租户的 TenantLineInnerInterceptor
            // 也方便注入其他自定义的 InnerInterceptor
            ObjectProvider<InnerInterceptor> innerInterceptorProvider) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        /*
        https://baomidou.com/plugins/
        使用多个插件时，需要注意它们的顺序。建议的顺序是：
        多租户、动态表名
        分页、乐观锁
        SQL 性能规范、防止全表更新与删除
        总结：对 SQL 进行单次改造的插件应优先放入，不对 SQL 进行改造的插件最后放入。
         */
        innerInterceptorProvider.orderedStream().forEach(interceptor::addInnerInterceptor);

        /*
        分页插件
         */
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
        // 设置 LIMIT 无上限
        paginationInnerInterceptor.setMaxLimit(-1L);
        interceptor.addInnerInterceptor(paginationInnerInterceptor);

        /*
        乐观锁
         */
        var subProps = props.getOptimisticLock();
        if (Boolean.TRUE.equals(subProps.getEnabled())) {
            interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        }

        /*
        防止全表更新与删除
         */
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        return interceptor;
    }

    /**
     * ID 生成器
     */
    @Bean
    @ConditionalOnMissingBean
    public IdentifierGenerator identifierGenerator(HelioIdGenProperties props) {
        return new HutoolSnowflakeIdGenerator(props);
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
    public TenantLineInnerInterceptor defaultTenantLineInnerInterceptor() {
        return new TenantLineInnerInterceptor(new TenantLineHandler() {
            @Override
            public Expression getTenantId() {
                return null;
            }
        });
    }

}
