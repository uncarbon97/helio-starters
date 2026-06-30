package cc.uncarbon.framework.helium.bizlog.autoconfigure;

import cc.uncarbon.framework.helium.bizlog.annotation.LogRecord;
import cc.uncarbon.framework.helium.bizlog.service.impl.DefaultDiffItemsToLogContentService;
import cc.uncarbon.framework.helium.bizlog.service.IDiffItemsToLogContentService;
import cc.uncarbon.framework.helium.bizlog.props.HeliumBizLogProperties;
import cc.uncarbon.framework.helium.bizlog.service.*;
import cc.uncarbon.framework.helium.bizlog.service.impl.*;
import cc.uncarbon.framework.helium.bizlog.support.aop.BeanFactoryLogRecordAdvisor;
import cc.uncarbon.framework.helium.bizlog.support.aop.LogRecordInterceptor;
import cc.uncarbon.framework.helium.bizlog.support.aop.LogRecordOperationSource;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Helium 业务日志自动装配类
 *
 * @author muzhantong@mzt-biz-log
 * @author Uncarbon
 */
@Conditional(value = HeliumBizLogAutoConfiguration.OnBizLogEnabled.class)
@EnableConfigurationProperties(value = {HeliumBizLogProperties.class})
@AutoConfiguration
@Slf4j
public class HeliumBizLogAutoConfiguration {

    private static final String BEAN_LOG_RECORD_INTERCEPTOR = "logRecordInterceptor";


    /**
     * 注解解析器：解析方法上的 {@link LogRecord}。
     *
     * @return 注解解析器
     */
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public LogRecordOperationSource logRecordOperationSource() {
        return new LogRecordOperationSource();
    }

    /**
     * 函数工厂：收集所有 {@link IParseFunction} 并按函数名建表。
     *
     * @param parseFunctions 容器中的自定义函数集合
     * @return 函数工厂
     */
    @Bean
    public ParseFunctionFactory parseFunctionFactory(@Autowired List<IParseFunction> parseFunctions) {
        return new ParseFunctionFactory(parseFunctions);
    }

    /**
     * diff 解析函数：注入差异转文案服务，并登记使用 equals 比较的类型。
     *
     * @param diffItemsToLogContentService 差异转文案服务
     * @param props                        日志配置
     * @return diff 解析函数
     */
    @Bean
    public DiffParseFunction diffParseFunction(IDiffItemsToLogContentService diffItemsToLogContentService,
                                               HeliumBizLogProperties props) {
        DiffParseFunction diffParseFunction = new DiffParseFunction();
        diffParseFunction.setDiffItemsToLogContentService(diffItemsToLogContentService);
        // issue#111
        diffParseFunction.addUseEqualsClass(LocalDateTime.class);
        if (StringUtils.hasText(props.getUseEqualsMethod())) {
            diffParseFunction.addUseEqualsClass(Arrays.asList(props.getUseEqualsMethod().split(",")));
        }
        return diffParseFunction;
    }

    /*
    ----------------------------------------------------------------
                        AOP
    ----------------------------------------------------------------
     */

    /**
     * AOP 拦截器
     *
     * @param props 日志配置
     * @return 日志拦截器
     */
    @Bean(value = BEAN_LOG_RECORD_INTERCEPTOR)
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public LogRecordInterceptor interceptor(HeliumBizLogProperties props) {
        return new LogRecordInterceptor()
                .setLogRecordOperationSource(logRecordOperationSource())
                .setTenant(props.getTenant())
                .setJoinTransaction(props.isJoinTransaction())
                .setDiffSameWhetherSaveLog(props.getDiffLog())
                .setLogRecordPerformanceMonitor(logRecordPerformanceMonitor());
    }

    /**
     * AOP 切面
     */
    @DependsOn(value = BEAN_LOG_RECORD_INTERCEPTOR)
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public BeanFactoryLogRecordAdvisor logRecordAdvisor(LogRecordInterceptor interceptor,
                                                        HeliumBizLogProperties props) {
        BeanFactoryLogRecordAdvisor advisor = new BeanFactoryLogRecordAdvisor();
        advisor.setLogRecordOperationSource(logRecordOperationSource());
        advisor.setAdvice(interceptor);
        advisor.setOrder(props.getOrder());
        return advisor;
    }

    /*
    ----------------------------------------------------------------
                        默认实现
    ----------------------------------------------------------------
     */

    /**
     * 自定义函数服务（容器中无自定义实现时生效）。
     *
     * @param parseFunctionFactory 函数工厂
     * @return 默认函数服务
     */
    @Bean
    @ConditionalOnMissingBean(IFunctionService.class)
    public IFunctionService functionService(ParseFunctionFactory parseFunctionFactory) {
        return new DefaultFunctionServiceImpl(parseFunctionFactory);
    }

    /**
     * 兜底空函数（容器中无自定义实现时生效）。
     *
     * @return 默认解析函数
     */
    @Bean
    @ConditionalOnMissingBean(IParseFunction.class)
    public DefaultParseFunction parseFunction() {
        return new DefaultParseFunction();
    }

    /**
     * 性能监控器（容器中无自定义实现时生效）。
     *
     * @return 默认性能监控器
     */
    @Bean
    @ConditionalOnMissingBean(ILogRecordPerformanceMonitor.class)
    public ILogRecordPerformanceMonitor logRecordPerformanceMonitor() {
        return new DefaultLogRecordPerformanceMonitor();
    }

    /**
     * 差异转文案服务（容器中无自定义实现时生效）。
     *
     * @param heliumBizLogProperties 日志配置
     * @return 默认差异转文案服务
     */
    @Bean
    @ConditionalOnMissingBean(IDiffItemsToLogContentService.class)
    @Role(BeanDefinition.ROLE_APPLICATION)
    public IDiffItemsToLogContentService diffItemsToLogContentService(HeliumBizLogProperties heliumBizLogProperties) {
        return new DefaultDiffItemsToLogContentService(heliumBizLogProperties);
    }

    /**
     * 操作人服务（容器中无自定义实现时生效）。
     *
     * @return 默认操作人服务
     */
    @Bean
    @ConditionalOnMissingBean(IOperatorGetService.class)
    @Role(BeanDefinition.ROLE_APPLICATION)
    public IOperatorGetService operatorGetService() {
        return new DefaultOperatorGetServiceImpl();
    }

    /**
     * 日志落库服务（容器中无自定义实现时生效）。
     *
     * @return 默认日志落库服务
     */
    @Bean
    @ConditionalOnMissingBean(ILogRecordDataService.class)
    @Role(BeanDefinition.ROLE_APPLICATION)
    public ILogRecordDataService recordDataService() {
        return new DefaultLogRecordDataServiceImpl();
    }

    static class OnBizLogEnabled implements Condition {
        @Override
        public boolean matches(ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {
            var props = Objects.requireNonNull(context.getBeanFactory()).getBean(HeliumBizLogProperties.class);
            return Boolean.TRUE.equals(props.getEnabled());
        }
    }
}
