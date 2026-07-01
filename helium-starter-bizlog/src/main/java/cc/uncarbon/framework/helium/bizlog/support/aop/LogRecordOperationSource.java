package cc.uncarbon.framework.helium.bizlog.support.aop;

import cc.uncarbon.framework.helium.bizlog.annotation.LogRecord;
import cc.uncarbon.framework.helium.bizlog.annotation.LogRecords;
import cc.uncarbon.framework.helium.bizlog.model.LogRecordOps;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
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
        HashSet<LogRecordOps> result = new HashSet<>(logRecordOps.size() + logRecordsOps.size()
                + abstractLogRecordOps.size() + abstractLogRecordsOps.size());
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
     * 解析元素上的所有 {@link LogRecord} 注解，同时支持**组合注解**。
     * <p>
     * 遍历元素上直接出现的注解：
     * <ul>
     *   <li>直接标注 {@link LogRecord} —— 按其自身属性解析；</li>
     *   <li>被 {@link LogRecord} 元标注的自定义注解（如 {@code @SysOperateLog}）—— 以内嵌
     *       {@link LogRecord} 为基线，用组合注解上<b>同名且显式赋值</b>的属性覆盖，
     *       从而实现「基于 {@code @LogRecord} 扩展、preset 部分属性、其余照常填」的效果，
     *       无需用户书写 {@code @AliasFor}。</li>
     * </ul>
     * 容器注解 {@link LogRecords} 由 {@link #parseLogRecordsAnnotations} 单独处理，此处不重复。
     *
     * @param ae 被注解元素
     * @return 日志操作集合
     */
    private Collection<LogRecordOps> parseLogRecordAnnotations(AnnotatedElement ae) {
        Collection<LogRecordOps> ret = new ArrayList<>();
        for (Annotation ann : ae.getAnnotations()) {
            Class<? extends Annotation> type = ann.annotationType();
            if (type == LogRecord.class) {
                ret.add(parseLogRecordAnnotation(ae, (LogRecord) ann));
            } else if (type != LogRecords.class) {
                // 组合注解：注解类型本身是否被 @LogRecord 元标注
                LogRecord embedded = AnnotatedElementUtils.findMergedAnnotation(type, LogRecord.class);
                if (embedded != null) {
                    ret.add(parseComposedLogRecord(ae, ann, embedded));
                }
            }
        }
        return ret;
    }

    /**
     * 将组合注解解析为 {@link LogRecordOps}。
     * <p>
     * 以组合注解类型上内嵌的 {@link LogRecord} 作为基线，逐个属性用组合注解<b>同名</b>属性覆盖；
     * 仅当组合注解显式赋值（与组合注解自身默认值不同）时才覆盖，否则保留内嵌值。
     *
     * @param ae       被注解元素（用于校验报错定位）
     * @param wrapper  组合注解实例
     * @param embedded 组合注解类型上内嵌的 {@link LogRecord}
     * @return 日志操作对象
     */
    private LogRecordOps parseComposedLogRecord(AnnotatedElement ae, Annotation wrapper, LogRecord embedded) {
        LogRecordOps recordOps = LogRecordOps.builder()
                .namespace(resolveAttr(wrapper, "namespace", embedded.namespace()))
                .bizType(resolveAttr(wrapper, "bizType", embedded.bizType()))
                .bizNo(resolveAttr(wrapper, "bizNo", embedded.bizNo()))
                .successLogTemplate(resolveAttr(wrapper, "success", embedded.success()))
                .failLogTemplate(resolveAttr(wrapper, "fail", embedded.fail()))
                .behavior(resolveAttr(wrapper, "behavior", embedded.behavior()))
                .operatorId(resolveAttr(wrapper, "operator", embedded.operator()))
                .extra(resolveAttr(wrapper, "extra", embedded.extra()))
                .condition(resolveAttr(wrapper, "condition", embedded.condition()))
                .isSuccess(resolveAttr(wrapper, "successCondition", embedded.successCondition()))
                .build();
        validateLogRecordOperation(ae, recordOps);
        return recordOps;
    }

    /**
     * 读取组合注解上的同名属性：若组合注解声明了该 {@code String} 属性且<b>显式赋值</b>（与组合注解自身默认值不同），
     * 则用组合注解的值；否则回退到内嵌 {@link LogRecord} 的值。
     *
     * @param wrapper     组合注解实例
     * @param attrName    属性名（与 {@link LogRecord} 的属性同名）
     * @param embeddedVal 内嵌 {@link LogRecord} 的属性值（回退值）
     * @return 最终生效的属性值
     */
    private String resolveAttr(Annotation wrapper, String attrName, String embeddedVal) {
        try {
            Method m = wrapper.annotationType().getDeclaredMethod(attrName);
            if (m.getReturnType() == String.class) {
                Object val = m.invoke(wrapper);
                Object dft = AnnotationUtils.getDefaultValue(wrapper, attrName);
                if (!Objects.equals(val, dft)) {
                    return (String) val;
                }
            }
        } catch (NoSuchMethodException ignored) {
            // 组合注解未声明该属性，回退到内嵌值
        } catch (ReflectiveOperationException e) {
            // 反射调用失败，保守回退到内嵌值
        }
        return embeddedVal;
    }

    /**
     * 将单个 {@link LogRecord} 注解映射为 {@link LogRecordOps}，并校验配置。
     *
     * @param ae         被注解元素
     * @param annotation 注解
     * @return 日志操作对象
     */
    private LogRecordOps parseLogRecordAnnotation(AnnotatedElement ae, LogRecord annotation) {
        LogRecordOps recordOps = LogRecordOps.builder()
                .namespace(annotation.namespace())
                .successLogTemplate(annotation.success())
                .failLogTemplate(annotation.fail())
                .bizType(annotation.bizType())
                .behavior(annotation.behavior())
                .operatorId(annotation.operator())
                .bizNo(annotation.bizNo())
                .extra(annotation.extra())
                .condition(annotation.condition())
                .isSuccess(annotation.successCondition())
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
            throw new IllegalStateException("Invalid annotation on '" +
                    ae.toString() + "'. 'one of successTemplate and failLogTemplate' attribute must be set.");
        }
    }

}
