package cc.uncarbon.framework.helium.bizlog.service.impl;

import cc.uncarbon.framework.helium.bizlog.context.LogRecordContext;
import cc.uncarbon.framework.helium.bizlog.service.IDiffItemsToLogContentService;
import cc.uncarbon.framework.helium.bizlog.support.diff.ArrayDiffer;
import de.danielbechler.diff.ObjectDifferBuilder;
import de.danielbechler.diff.comparison.ComparisonService;
import de.danielbechler.diff.node.DiffNode;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 对象差异（diff）解析函数
 * <p>
 * 内置自定义函数 {@code _DIFF}，基于 {@code java-object-diff} 对两个对象进行字段级差异比对，
 * 并将差异结果交由 {@link IDiffItemsToLogContentService} 转换为日志文案。
 *
 * @author muzhantong@mzt-biz-log
 * @author Uncarbon
 */
@Slf4j
public class DiffParseFunction {

    /**
     * diff 自定义函数名，对应模板中的 {@code {_DIFF{...}}}
     */
    public static final String diffFunctionName = "_DIFF";

    /**
     * 单参 diff 时，旧对象在 {@link LogRecordContext} 中的变量名
     */
    public static final String OLD_OBJECT = "_oldObj";

    /**
     * -- SETTER --
     *  注入差异节点转文案服务。
     */
    @Setter
    private IDiffItemsToLogContentService diffItemsToLogContentService;

    /**
     * 需强制使用 {@code equals} 比较的类型集合（如 BigDecimal 等不宜反射逐字段比较的类型）
     */
    private final Set<Class<?>> comparisonSet = new HashSet<>();

    /**
     * 比较两个对象并生成差异文案。
     * <p>
     * source/target 任一为空时，反射调用无参构造补一个空对象后再比较；
     * 两对象类型不同时记 error 日志并返回空串。
     *
     * @param source 旧对象
     * @param target 新对象
     * @return 差异文案；无可比内容时返回空串
     */
    public String diff(Object source, Object target) {
        if (source == null && target == null) {
            return "";
        }
        if (source == null || target == null) {
            try {
                Class<?> clazz = source == null ? target.getClass() : source.getClass();
                source = source == null ? clazz.getDeclaredConstructor().newInstance() : source;
                target = target == null ? clazz.getDeclaredConstructor().newInstance() : target;
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                     InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        if (!Objects.equals(AopUtils.getTargetClass(source.getClass()), AopUtils.getTargetClass(target.getClass()))) {
            log.error("diff的两个对象类型不同, source.class={}, target.class={}", source.getClass().toString(), target.getClass().toString());
            return "";
        }
        ObjectDifferBuilder objectDifferBuilder = ObjectDifferBuilder.startBuilding();
        ObjectDifferBuilder register = objectDifferBuilder
                .differs().register((differDispatcher, nodeQueryService) ->
                        new ArrayDiffer(differDispatcher, (ComparisonService) objectDifferBuilder.comparison(), objectDifferBuilder.identity()));
        for (Class<?> clazz : comparisonSet) {
            register.comparison().ofType(clazz).toUseEqualsMethod();
        }
        DiffNode diffNode = register.build().compare(target, source);
        return diffItemsToLogContentService.toLogContent(diffNode, source, target);
    }

    /**
     * 单参 diff：旧对象从 {@link LogRecordContext} 取（变量名 {@link #OLD_OBJECT}）。
     *
     * @param newObj 新对象
     * @return 差异文案
     */
    public String diff(Object newObj) {
        Object oldObj = LogRecordContext.getMethodOrShared(OLD_OBJECT);
        return diff(oldObj, newObj);
    }

    /**
     * 批量登记使用 {@code equals} 比较的类型（按全限定类名）。
     *
     * @param classList 类全限定名集合
     */
    public void addUseEqualsClass(List<String> classList) {
        if (classList != null && !classList.isEmpty()) {
            for (String clazz : classList) {
                try {
                    Class<?> aClass = Class.forName(clazz);
                    comparisonSet.add(aClass);
                } catch (ClassNotFoundException e) {
                    log.warn("无效的比对类型, className={}", clazz);
                }
            }
        }
    }

    /**
     * 登记使用 {@code equals} 比较的类型。
     *
     * @param clazz 类型
     */
    public void addUseEqualsClass(Class<?> clazz) {
        comparisonSet.add(clazz);
    }
}
