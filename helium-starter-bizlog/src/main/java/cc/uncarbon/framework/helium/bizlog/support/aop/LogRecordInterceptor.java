package cc.uncarbon.framework.helium.bizlog.support.aop;

import cc.uncarbon.framework.helium.bizlog.beans.CodeVariableType;
import cc.uncarbon.framework.helium.bizlog.beans.LogRecord;
import cc.uncarbon.framework.helium.bizlog.beans.LogRecordOps;
import cc.uncarbon.framework.helium.bizlog.beans.MethodExecuteResult;
import cc.uncarbon.framework.helium.bizlog.context.LogRecordContext;
import cc.uncarbon.framework.helium.bizlog.service.IFunctionService;
import cc.uncarbon.framework.helium.bizlog.service.ILogRecordPerformanceMonitor;
import cc.uncarbon.framework.helium.bizlog.service.ILogRecordService;
import cc.uncarbon.framework.helium.bizlog.service.IOperatorGetService;
import cc.uncarbon.framework.helium.bizlog.service.impl.DiffParseFunction;
import cc.uncarbon.framework.helium.bizlog.support.parse.LogFunctionParser;
import cc.uncarbon.framework.helium.bizlog.support.parse.LogRecordValueParser;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;


/**
 * 业务日志 AOP 拦截器
 * <p>
 * 拦截标注了 {@code @LogRecord} 的方法，依次完成：
 * 解析注解、执行前置自定义函数、执行目标方法、按成功/失败模板渲染日志文案、落库，
 * 并在拦截入口绑定 {@link LogRecordContext} 日志上下文作用域（支持虚拟线程传递）。
 *
 * @author mzt@mzt-biz-log
 * @author Uncarbon
 */
@Slf4j
public class LogRecordInterceptor extends LogRecordValueParser implements MethodInterceptor, Serializable, SmartInitializingSingleton {

    private LogRecordOperationSource logRecordOperationSource;

    private String tenantId;

    private ILogRecordService bizLogService;

    private IOperatorGetService operatorGetService;

    private ILogRecordPerformanceMonitor logRecordPerformanceMonitor;

    private boolean joinTransaction;

    /**
     * 拦截方法入口。
     *
     * @param invocation 方法调用上下文
     * @return 目标方法返回值
     * @throws Throwable 目标方法或日志处理抛出的异常
     */
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        return execute(invocation, invocation.getThis(), method, invocation.getArguments());
    }

    /**
     * 拦截主流程：穿透代理对象、绑定日志上下文作用域，并委托 {@link #doExecute} 执行。
     * <p>
     * 顶层调用通过 {@link LogRecordContext#callWithContextThrowing} 绑定全新作用域，
     * 嵌套调用复用外层已绑定的作用域，保证方法级 span 栈与全局变量语义正确。
     *
     * @param invoker 方法调用上下文
     * @param target  目标对象
     * @param method  目标方法
     * @param args    方法参数
     * @return 目标方法返回值
     * @throws Throwable 目标方法或日志处理抛出的异常
     */
    private Object execute(MethodInvocation invoker, Object target, Method method, Object[] args) throws Throwable {
        // 代理对象不再拦截自身，避免重复代理
        if (AopUtils.isAopProxy(target)) {
            return invoker.proceed();
        }
        if (LogRecordContext.isBound()) {
            // 嵌套调用：复用外层已绑定的日志上下文作用域
            return doExecute(invoker, target, method, args);
        }
        // 顶层调用：绑定全新的日志上下文作用域，退出时自动解绑
        return LogRecordContext.callWithContextThrowing(() -> doExecute(invoker, target, method, args));
    }

    /**
     * 实际的日志记录执行逻辑：前置函数解析 → 执行目标方法 → 模板渲染与落库。
     *
     * @param invoker 方法调用上下文
     * @param target  目标对象
     * @param method  目标方法
     * @param args    方法参数
     * @return 目标方法返回值
     * @throws Throwable 目标方法或日志处理抛出的异常
     */
    private Object doExecute(MethodInvocation invoker, Object target, Method method, Object[] args) throws Throwable {
        StopWatch stopWatch = new StopWatch(ILogRecordPerformanceMonitor.MONITOR_NAME);
        stopWatch.start(ILogRecordPerformanceMonitor.MONITOR_TASK_BEFORE_EXECUTE);
        Class<?> targetClass = getTargetClass(target);
        Object ret = null;
        MethodExecuteResult methodExecuteResult = new MethodExecuteResult(method, args, targetClass);
        LogRecordContext.putEmptySpan();
        Collection<LogRecordOps> operations = new ArrayList<>();
        Map<String, String> functionNameAndReturnMap = new HashMap<>();
        try {
            operations = logRecordOperationSource.computeLogRecordOperations(method, targetClass);
            List<String> spElTemplates = getBeforeExecuteFunctionTemplate(operations);
            functionNameAndReturnMap = processBeforeExecuteFunctionTemplate(spElTemplates, targetClass, method, args);
        } catch (Exception e) {
            log.error("log record parse before function exception", e);
        } finally {
            stopWatch.stop();
        }

        try {
            ret = invoker.proceed();
            methodExecuteResult.setResult(ret);
            methodExecuteResult.setSuccess(true);
        } catch (Exception e) {
            methodExecuteResult.setSuccess(false);
            methodExecuteResult.setThrowable(e);
            methodExecuteResult.setErrorMsg(e.getMessage());
        }
        stopWatch.start(ILogRecordPerformanceMonitor.MONITOR_TASK_AFTER_EXECUTE);
        try {
            if (!CollectionUtils.isEmpty(operations)) {
                recordExecute(methodExecuteResult, functionNameAndReturnMap, operations);
            }
        } catch (Exception t) {
            log.error("log record parse exception", t);
            throw t;
        } finally {
            LogRecordContext.clear();
            stopWatch.stop();
            try {
                logRecordPerformanceMonitor.print(stopWatch);
            } catch (Exception e) {
                log.error("execute exception", e);
            }
        }

        if (methodExecuteResult.getThrowable() != null) {
            throw methodExecuteResult.getThrowable();
        }
        return ret;
    }

    /**
     * 收集所有操作的成功模板中需要前置执行的自定义函数 SpEL 模板（失败模板不参与前置解析）。
     *
     * @param operations 当前方法解析出的日志操作集合
     * @return 前置函数 SpEL 模板列表
     */
    private List<String> getBeforeExecuteFunctionTemplate(Collection<LogRecordOps> operations) {
        List<String> spElTemplates = new ArrayList<>();
        for (LogRecordOps operation : operations) {
            //执行之前的函数，失败模版不解析
            List<String> templates = getSpElTemplates(operation, operation.getSuccessLogTemplate());
            if (!CollectionUtils.isEmpty(templates)) {
                spElTemplates.addAll(templates);
            }
        }
        return spElTemplates;
    }

    /**
     * 逐个操作执行日志记录：跳过未命中条件或不满足成功条件的操作，按结果走成功或失败模板。
     *
     * @param methodExecuteResult         方法执行结果
     * @param functionNameAndReturnMap    前置函数返回值缓存
     * @param operations                  日志操作集合
     */
    private void recordExecute(MethodExecuteResult methodExecuteResult, Map<String, String> functionNameAndReturnMap,
                               Collection<LogRecordOps> operations) {
        for (LogRecordOps operation : operations) {
            try {
                if (StringUtils.isEmpty(operation.getSuccessLogTemplate())
                        && StringUtils.isEmpty(operation.getFailLogTemplate())) {
                    continue;
                }
                if (exitsCondition(methodExecuteResult, functionNameAndReturnMap, operation)) continue;
                if (!methodExecuteResult.isSuccess()) {
                    failRecordExecute(methodExecuteResult, functionNameAndReturnMap, operation);
                } else {
                    successRecordExecute(methodExecuteResult, functionNameAndReturnMap, operation);
                }
            } catch (Exception t) {
                log.error("log record execute exception", t);
                if (joinTransaction) throw t;
            }
        }
    }

    /**
     * 业务方法执行成功后的日志记录：解析成功/失败模板并落库。
     *
     * @param methodExecuteResult      方法执行结果
     * @param functionNameAndReturnMap 前置函数返回值缓存
     * @param operation                当前日志操作
     */
    private void successRecordExecute(MethodExecuteResult methodExecuteResult, Map<String, String> functionNameAndReturnMap,
                                      LogRecordOps operation) {
        // 若存在 isSuccess 条件模版，解析出成功/失败的模版
        String action = "";
        boolean flag = true;
        if (!StringUtils.isEmpty(operation.getIsSuccess())) {
            String condition = singleProcessTemplate(methodExecuteResult, operation.getIsSuccess(), functionNameAndReturnMap);
            if (StringUtils.endsWithIgnoreCase(condition, "true")) {
                action = operation.getSuccessLogTemplate();
            } else {
                action = operation.getFailLogTemplate();
                flag = false;
            }
        } else {
            action = operation.getSuccessLogTemplate();
        }
        if (StringUtils.isEmpty(action)) {
            // 没有日志内容则忽略
            return;
        }
        List<String> spElTemplates = getSpElTemplates(operation, action);
        String operatorIdFromService = getOperatorIdFromServiceAndPutTemplate(operation, spElTemplates);
        Map<String, String> expressionValues = processTemplate(spElTemplates, methodExecuteResult, functionNameAndReturnMap);
        saveLog(methodExecuteResult.getMethod(), !flag, operation, operatorIdFromService, action, expressionValues);
    }

    /**
     * 业务方法执行失败后的日志记录：解析失败模板并落库。
     *
     * @param methodExecuteResult      方法执行结果
     * @param functionNameAndReturnMap 前置函数返回值缓存
     * @param operation                当前日志操作
     */
    private void failRecordExecute(MethodExecuteResult methodExecuteResult, Map<String, String> functionNameAndReturnMap,
                                   LogRecordOps operation) {
        if (StringUtils.isEmpty(operation.getFailLogTemplate())) return;

        String action = operation.getFailLogTemplate();
        List<String> spElTemplates = getSpElTemplates(operation, action);
        String operatorIdFromService = getOperatorIdFromServiceAndPutTemplate(operation, spElTemplates);

        Map<String, String> expressionValues = processTemplate(spElTemplates, methodExecuteResult, functionNameAndReturnMap);
        saveLog(methodExecuteResult.getMethod(), true, operation, operatorIdFromService, action, expressionValues);
    }

    /**
     * 判断当前操作是否因 {@code condition} 不满足而应跳过。
     * <p>
     * 注意：方法名为历史遗留拼写，语义为「是否退出（exit）当前操作的记录」。
     *
     * @param methodExecuteResult      方法执行结果
     * @param functionNameAndReturnMap 前置函数返回值缓存
     * @param operation                当前日志操作
     * @return 需跳过返回 {@code true}
     */
    private boolean exitsCondition(MethodExecuteResult methodExecuteResult,
                                   Map<String, String> functionNameAndReturnMap, LogRecordOps operation) {
        if (!StringUtils.isEmpty(operation.getCondition())) {
            String condition = singleProcessTemplate(methodExecuteResult, operation.getCondition(), functionNameAndReturnMap);
            if (StringUtils.endsWithIgnoreCase(condition, "false")) return true;
        }
        return false;
    }

    /**
     * 渲染并落库一条业务日志。
     *
     * @param method                 目标方法
     * @param flag                   是否为失败日志
     * @param operation              当前日志操作
     * @param operatorIdFromService  由操作人服务解析到的操作人ID
     * @param action                 待渲染的文案模板
     * @param expressionValues       模板变量解析结果
     */
    private void saveLog(Method method, boolean flag, LogRecordOps operation, String operatorIdFromService,
                         String action, Map<String, String> expressionValues) {
        if (StringUtils.isEmpty(expressionValues.get(action)) ||
                (!diffSameWhetherSaveLog && action.contains("#") && Objects.equals(action, expressionValues.get(action)))) {
            return;
        }
        LogRecord logRecord = LogRecord.builder()
                .tenant(tenantId)
                .type(expressionValues.get(operation.getType()))
                .bizNo(expressionValues.get(operation.getBizNo()))
                .operator(getRealOperatorId(operation, operatorIdFromService, expressionValues))
                .subType(expressionValues.get(operation.getSubType()))
                .extra(expressionValues.get(operation.getExtra()))
                .codeVariable(getCodeVariable(method))
                .action(expressionValues.get(action))
                .fail(flag)
                .createTime(new Date())
                .build();

        bizLogService.record(logRecord);
    }

    /**
     * 构造代码定位信息：类名 + 方法名。
     *
     * @param method 目标方法
     * @return 代码定位信息
     */
    private Map<CodeVariableType, Object> getCodeVariable(Method method) {
        Map<CodeVariableType, Object> map = new HashMap<>();
        map.put(CodeVariableType.ClassName, method.getDeclaringClass());
        map.put(CodeVariableType.MethodName, method.getName());
        return map;
    }

    /**
     * 汇总需要解析的 SpEL 模板：固定解析 type/bizNo/subType/extra，再附加传入的文案模板。
     *
     * @param operation 当前日志操作
     * @param actions   文案模板（成功或失败）
     * @return 待解析的 SpEL 模板列表
     */
    private List<String> getSpElTemplates(LogRecordOps operation, String... actions) {
        List<String> spElTemplates = new ArrayList<>();
        spElTemplates.add(operation.getType());
        spElTemplates.add(operation.getBizNo());
        spElTemplates.add(operation.getSubType());
        spElTemplates.add(operation.getExtra());
        spElTemplates.addAll(Arrays.asList(actions));
        return spElTemplates;
    }

    /**
     * 取最终操作人ID：优先使用操作人服务解析结果，否则取模板解析结果。
     *
     * @param operation              当前日志操作
     * @param operatorIdFromService  由操作人服务解析到的操作人ID
     * @param expressionValues       模板变量解析结果
     * @return 最终操作人ID
     */
    private String getRealOperatorId(LogRecordOps operation, String operatorIdFromService, Map<String, String> expressionValues) {
        return !StringUtils.isEmpty(operatorIdFromService) ? operatorIdFromService : expressionValues.get(operation.getOperatorId());
    }

    /**
     * 解析操作人：若注解未声明 operator 则走 {@link IOperatorGetService}，否则将 operator 模板加入待解析列表。
     *
     * @param operation     当前日志操作
     * @param spElTemplates 待解析的 SpEL 模板列表
     * @return 操作人服务解析到的操作人ID（未走服务时为空串）
     */
    private String getOperatorIdFromServiceAndPutTemplate(LogRecordOps operation, List<String> spElTemplates) {

        String realOperatorId = "";
        if (StringUtils.isEmpty(operation.getOperatorId())) {
            realOperatorId = operatorGetService.getUser().getOperatorId();
            if (StringUtils.isEmpty(realOperatorId)) {
                throw new IllegalArgumentException("[LogRecord] operator is null");
            }
        } else {
            spElTemplates.add(operation.getOperatorId());
        }
        return realOperatorId;
    }

    /**
     * 取目标对象的最终类型（穿透代理）。
     *
     * @param target 目标对象
     * @return 目标类型
     */
    private Class<?> getTargetClass(Object target) {
        return AopProxyUtils.ultimateTargetClass(target);
    }


    /**
     * 注入日志操作注解解析器。
     *
     * @param logRecordOperationSource 注解解析器
     */
    public void setLogRecordOperationSource(LogRecordOperationSource logRecordOperationSource) {
        this.logRecordOperationSource = logRecordOperationSource;
    }

    /**
     * 注入租户标识。
     *
     * @param tenant 租户标识
     */
    public void setTenant(String tenant) {
        this.tenantId = tenant;
    }

    /**
     * 注入日志落库服务。
     *
     * @param bizLogService 日志落库服务
     */
    public void setLogRecordService(ILogRecordService bizLogService) {
        this.bizLogService = bizLogService;
    }

    /**
     * 注入性能监控器。
     *
     * @param logRecordPerformanceMonitor 性能监控器
     */
    public void setLogRecordPerformanceMonitor(ILogRecordPerformanceMonitor logRecordPerformanceMonitor) {
        this.logRecordPerformanceMonitor = logRecordPerformanceMonitor;
    }

    /**
     * 设置是否跟随业务事务（日志异常时是否回滚业务事务）。
     *
     * @param joinTransaction 是否加入业务事务
     */
    public void setJoinTransaction(boolean joinTransaction) {
        this.joinTransaction = joinTransaction;
    }

    /**
     * 设置 diff 结果与原值相同时是否仍落库。
     *
     * @param diffLog 是否落相同 diff
     */
    public void setDiffSameWhetherSaveLog(boolean diffLog) {
        this.diffSameWhetherSaveLog = diffLog;
    }

    /**
     * 单例全部就绪后，从容器获取依赖的各服务并注入解析器。
     */
    @Override
    public void afterSingletonsInstantiated() {
        bizLogService = beanFactory.getBean(ILogRecordService.class);
        operatorGetService = beanFactory.getBean(IOperatorGetService.class);
        this.setLogFunctionParser(new LogFunctionParser(beanFactory.getBean(IFunctionService.class)));
        this.setDiffParseFunction(beanFactory.getBean(DiffParseFunction.class));
    }
}
