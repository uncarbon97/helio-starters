package cc.uncarbon.framework.helium.bizlog.configuration;

import cc.uncarbon.framework.helium.bizlog.props.LogRecordProperties;
import cc.uncarbon.framework.helium.bizlog.service.*;
import cc.uncarbon.framework.helium.bizlog.service.impl.*;
import cc.uncarbon.framework.helium.bizlog.diff.DefaultDiffItemsToLogContentService;
import cc.uncarbon.framework.helium.bizlog.diff.IDiffItemsToLogContentService;
import cc.uncarbon.framework.helium.bizlog.support.aop.BeanFactoryLogRecordAdvisor;
import cc.uncarbon.framework.helium.bizlog.support.aop.LogRecordInterceptor;
import cc.uncarbon.framework.helium.bizlog.support.aop.LogRecordOperationSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 业务日志自动装配
 * <p>
 * 注册 AOP 切面、拦截器、注解解析器以及各默认服务（函数服务、diff、操作人、落库、性能监控）。
 * 使用方可通过实现对应接口覆盖默认行为。
 *
 * @author muzhantong@mzt-biz-log
 * @author Uncarbon
 */
@Configuration
@EnableConfigurationProperties({LogRecordProperties.class})
@Slf4j
public class LogRecordProxyAutoConfiguration implements ImportAware {

    private AnnotationAttributes enableLogRecord;


    /**
     * 注解解析器：解析方法上的 {@code @LogRecord}。
     *
     * @return 注解解析器
     */
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public LogRecordOperationSource logRecordOperationSource() {
        return new LogRecordOperationSource();
    }

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
     * 日志切面：组合 Pointcut 与拦截器。
     *
     * @param logRecordInterceptor 日志拦截器
     * @return 日志切面
     */
    @DependsOn("logRecordInterceptor")
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public BeanFactoryLogRecordAdvisor logRecordAdvisor(LogRecordInterceptor logRecordInterceptor) {
        BeanFactoryLogRecordAdvisor advisor =
                new BeanFactoryLogRecordAdvisor();
        advisor.setLogRecordOperationSource(logRecordOperationSource());
        advisor.setAdvice(logRecordInterceptor);
        advisor.setOrder(enableLogRecord.getNumber("order"));
        return advisor;
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
     * 日志拦截器：装配注解解析器、租户、事务、性能监控等依赖。
     *
     * @param logRecordProperties 日志配置
     * @return 日志拦截器
     */
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public LogRecordInterceptor logRecordInterceptor(LogRecordProperties logRecordProperties) {
        LogRecordInterceptor interceptor = new LogRecordInterceptor();
        interceptor.setLogRecordOperationSource(logRecordOperationSource());
        interceptor.setTenant(enableLogRecord.getString("tenant"));
        interceptor.setJoinTransaction(enableLogRecord.getBoolean("joinTransaction"));
        interceptor.setDiffSameWhetherSaveLog(logRecordProperties.getDiffLog());
        //interceptor.setLogFunctionParser(logFunctionParser(functionService));
        //interceptor.setDiffParseFunction(diffParseFunction);
        interceptor.setLogRecordPerformanceMonitor(logRecordPerformanceMonitor());
        return interceptor;
    }

//    @Bean
//    public LogFunctionParser logFunctionParser(IFunctionService functionService) {
//        return new LogFunctionParser(functionService);
//    }

    /**
     * diff 解析函数：注入差异转文案服务，并登记使用 equals 比较的类型。
     *
     * @param diffItemsToLogContentService 差异转文案服务
     * @param logRecordProperties          日志配置
     * @return diff 解析函数
     */
    @Bean
    public DiffParseFunction diffParseFunction(IDiffItemsToLogContentService diffItemsToLogContentService,
                                               LogRecordProperties logRecordProperties) {
        DiffParseFunction diffParseFunction = new DiffParseFunction();
        diffParseFunction.setDiffItemsToLogContentService(diffItemsToLogContentService);
        // issue#111
        diffParseFunction.addUseEqualsClass(LocalDateTime.class);
        if (!StringUtils.isEmpty(logRecordProperties.getUseEqualsMethod())) {
            diffParseFunction.addUseEqualsClass(Arrays.asList(logRecordProperties.getUseEqualsMethod().split(",")));
        }
        return diffParseFunction;
    }

    /**
     * 差异转文案服务（容器中无自定义实现时生效）。
     *
     * @param logRecordProperties 日志配置
     * @return 默认差异转文案服务
     */
    @Bean
    @ConditionalOnMissingBean(IDiffItemsToLogContentService.class)
    @Role(BeanDefinition.ROLE_APPLICATION)
    public IDiffItemsToLogContentService diffItemsToLogContentService(LogRecordProperties logRecordProperties) {
        return new DefaultDiffItemsToLogContentService(logRecordProperties);
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
    @ConditionalOnMissingBean(ILogRecordService.class)
    @Role(BeanDefinition.ROLE_APPLICATION)
    public ILogRecordService recordService() {
        return new DefaultLogRecordServiceImpl();
    }

    /**
     * 接收 {@link EnableLogRecord} 注解元数据，供各 Bean 装配时读取属性。
     *
     * @param importMetadata 导入元数据
     */
    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        this.enableLogRecord = AnnotationAttributes.fromMap(
                importMetadata.getAnnotationAttributes(EnableLogRecord.class.getName(), false));
        if (this.enableLogRecord == null) {
            log.info("EnableLogRecord is not present on importing class");
        }
    }
}
