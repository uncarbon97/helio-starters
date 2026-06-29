package cc.uncarbon.framework.helium.bizlog.support.aop;

import cc.uncarbon.framework.helium.bizlog.annotation.LogRecord;
import cc.uncarbon.framework.helium.bizlog.annotation.LogRecords;
import cc.uncarbon.framework.helium.bizlog.beans.LogRecordOps;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.StringUtils;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * 业务日志注解解析器
 * <p>
 * 解析目标方法（含接口、父类继承）上的 {@link LogRecord} / {@link LogRecords}，映射为 {@link LogRecordOps} 集合。
 *
 * @author mzt@mzt-biz-log
 * @author Uncarbon
 */
public class LogRecordOperationSource {
    /**
     * 接口等价方法缓存：声明类所实现接口上的等价方法。
     */
    private static final Map<Method, Method> INTERFACE_METHOD_CACHE = new ConcurrentReferenceHashMap<>(256);

    /**
     * 解析目标方法上的所有 {@link LogRecord} 操作（合并方法本身、接口方法上的注解）。
     *
     * @param method      目标方法
     * @param targetClass 目标类
     * @return 日志操作集合（无注解时为空集合）
     */
    public Collection<LogRecordOps> computeLogRecordOperations(Method method, Class<?> targetClass) {
        // 仅允许 public 方法
        if (!Modifier.isPublic(method.getModifiers())) {
            return Collections.emptyList();
        }

        // 方法可能定义在接口上，但需要从目标类取属性；目标类为空时方法不变
        Method specificMethod = ClassUtils.getMostSpecificMethod(method, targetClass);
        // 处理泛型参数桥接方法，还原原始方法
        specificMethod = BridgeMethodResolver.findBridgedMethod(specificMethod);

        // 先尝试目标类本身的方法
        Collection<LogRecordOps> logRecordOps = parseLogRecordAnnotations(specificMethod);
        Collection<LogRecordOps> logRecordsOps = parseLogRecordsAnnotations(specificMethod);
        Collection<LogRecordOps> abstractLogRecordOps = parseLogRecordAnnotations(getInterfaceMethodIfPossible(method));
        Collection<LogRecordOps> abstractLogRecordsOps = parseLogRecordsAnnotations(getInterfaceMethodIfPossible(method));
        HashSet<LogRecordOps> result = new HashSet<>();
        result.addAll(logRecordOps);
        result.addAll(abstractLogRecordOps);
        result.addAll(logRecordsOps);
        result.addAll(abstractLogRecordsOps);
        return result;
    }

    /**
     * 尝试找到给定方法在接口上对应的等价方法。
     * <p>
     * 在 Jigsaw 模块化下，反射调用接口上的公开类型方法可避免非法访问告警。
     *
     * @param method 待匹配的方法（可能来自实现类）
     * @return 对应的接口方法，找不到时返回原方法
     */
    public static Method getInterfaceMethodIfPossible(Method method) {
        if (!Modifier.isPublic(method.getModifiers()) || method.getDeclaringClass().isInterface()) {
            return method;
        }
        // 抽象类 + 接口 只会保留一个方法
        return INTERFACE_METHOD_CACHE.computeIfAbsent(method, key -> {
            Class<?> current = key.getDeclaringClass();
            while (current != null && current != Object.class) {
                for (Class<?> ifc : current.getInterfaces()) {
                    try {
                        return ifc.getMethod(key.getName(), key.getParameterTypes());
                    } catch (NoSuchMethodException ex) {
                        // 忽略，继续查找下一个接口
                    }
                }
                current = current.getSuperclass();
            }
            return key;
        });
    }

    /**
     * 解析元素上的 {@link LogRecords} 容器注解，逐个展开为 {@link LogRecordOps}。
     *
     * @param ae 被注解元素
     * @return 日志操作集合
     */
    private Collection<LogRecordOps> parseLogRecordsAnnotations(AnnotatedElement ae) {
        Collection<LogRecordOps> res = new ArrayList<>();
        Collection<LogRecords> logRecordAnnotationAnnotations = AnnotatedElementUtils.findAllMergedAnnotations(ae, LogRecords.class);
        if (!logRecordAnnotationAnnotations.isEmpty()) {
            logRecordAnnotationAnnotations.forEach(logRecords -> {
                LogRecord[] value = logRecords.value();
                for (LogRecord logRecord : value) {
                    res.add(parseLogRecordAnnotation(ae, logRecord));
                }
            });
        }
        return res;
    }

    /**
     * 解析元素上的所有 {@link LogRecord} 注解。
     *
     * @param ae 被注解元素
     * @return 日志操作集合
     */
    private Collection<LogRecordOps> parseLogRecordAnnotations(AnnotatedElement ae) {
        Collection<LogRecord> logRecordAnnotationAnnotations = AnnotatedElementUtils.findAllMergedAnnotations(ae, LogRecord.class);
        Collection<LogRecordOps> ret = new ArrayList<>();
        if (!logRecordAnnotationAnnotations.isEmpty()) {
            for (LogRecord recordAnnotation : logRecordAnnotationAnnotations) {
                ret.add(parseLogRecordAnnotation(ae, recordAnnotation));
            }
        }
        return ret;
    }

    /**
     * 将单个 {@link LogRecord} 注解映射为 {@link LogRecordOps}，并校验配置。
     *
     * @param ae              被注解元素
     * @param recordAnnotation 注解
     * @return 日志操作对象
     */
    private LogRecordOps parseLogRecordAnnotation(AnnotatedElement ae, LogRecord recordAnnotation) {
        LogRecordOps recordOps = LogRecordOps.builder()
                .successLogTemplate(recordAnnotation.success())
                .failLogTemplate(recordAnnotation.fail())
                .type(recordAnnotation.type())
                .bizNo(recordAnnotation.bizNo())
                .operatorId(recordAnnotation.operator())
                .subType(recordAnnotation.subType())
                .extra(recordAnnotation.extra())
                .condition(recordAnnotation.condition())
                .isSuccess(recordAnnotation.successCondition())
                .build();
        validateLogRecordOperation(ae, recordOps);
        return recordOps;
    }


    /**
     * 校验注解配置：成功模板与失败模板至少配置其一。
     *
     * @param ae        被注解元素
     * @param recordOps 日志操作对象
     */
    private void validateLogRecordOperation(AnnotatedElement ae, LogRecordOps recordOps) {
        if (!StringUtils.hasText(recordOps.getSuccessLogTemplate()) && !StringUtils.hasText(recordOps.getFailLogTemplate())) {
            throw new IllegalStateException("Invalid logRecord annotation configuration on '" +
                    ae.toString() + "'. 'one of successTemplate and failLogTemplate' attribute must be set.");
        }
    }

}
