package cc.uncarbon.framework.helium.bizlog.diff;

import cc.uncarbon.framework.helium.bizlog.annotation.DIffLogIgnore;
import cc.uncarbon.framework.helium.bizlog.annotation.DiffLogAllFields;
import cc.uncarbon.framework.helium.bizlog.annotation.DiffLogField;
import cc.uncarbon.framework.helium.bizlog.props.LogRecordProperties;
import cc.uncarbon.framework.helium.bizlog.service.IFunctionService;
import de.danielbechler.diff.node.DiffNode;
import de.danielbechler.diff.selector.ElementSelector;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 差异节点转文案默认实现
 * <p>
 * 遍历 {@link DiffNode} 差异树，按字段配置（{@link DiffLogField} / {@link DiffLogAllFields} / {@link DIffLogIgnore}）
 * 生成形如「字段名从【旧值】修改为【新值】」的日志文案，支持嵌套对象与集合。
 *
 * @author muzhantong@mzt-biz-log
 * @author Uncarbon
 */
@Slf4j
@Setter
@Getter
public class DefaultDiffItemsToLogContentService implements IDiffItemsToLogContentService, BeanFactoryAware, SmartInitializingSingleton {

    private IFunctionService functionService;
    private final LogRecordProperties logRecordProperties;
    private BeanFactory beanFactory;

    /**
     * 注入日志配置。
     *
     * @param logRecordProperties 日志配置
     */
    public DefaultDiffItemsToLogContentService(LogRecordProperties logRecordProperties) {
        this.logRecordProperties = logRecordProperties;
    }

    /**
     * 将差异节点转为日志文案；无变化时返回空串。
     *
     * @param diffNode     差异节点
     * @param sourceObject 旧对象
     * @param targetObject 新对象
     * @return 差异文案
     */
    @Override
    public String toLogContent(DiffNode diffNode, final Object sourceObject, final Object targetObject) {
        if (!diffNode.hasChanges()) {
            return "";
        }
        DiffLogAllFields annotation = sourceObject.getClass().getAnnotation(DiffLogAllFields.class);
        StringBuilder stringBuilder = new StringBuilder();
        Set<DiffNode> set = new HashSet<>();
        diffNode.visit((node, visit) -> generateAllFieldLog(sourceObject, targetObject, stringBuilder, node, annotation, set));
        set.clear();
        return stringBuilder.toString().replaceAll(logRecordProperties.getFieldSeparator().concat("$"), "");
    }

    /**
     * 针对单个差异节点生成字段级文案，并追加到结果中。
     *
     * @param sourceObject   旧对象
     * @param targetObject   新对象
     * @param stringBuilder  文案构建器
     * @param node           差异节点
     * @param annotation     类级 {@link DiffLogAllFields}（可能为空）
     * @param set            已处理节点集合（防重复）
     */
    private void generateAllFieldLog(Object sourceObject, Object targetObject, StringBuilder stringBuilder, DiffNode node,
                                     DiffLogAllFields annotation, Set<DiffNode> set) {
        if (node.isRootNode() || node.getValueTypeInfo() != null || set.contains(node)) {
            return;
        }
        DIffLogIgnore logIgnore = node.getFieldAnnotation(DIffLogIgnore.class);
        if (logIgnore != null) {
            memorandum(node, set);
            return;
        }
        DiffLogField diffLogFieldAnnotation = node.getFieldAnnotation(DiffLogField.class);
        if (annotation == null && diffLogFieldAnnotation == null) {
            return;
        }
        String filedLogName = getFieldLogName(node, diffLogFieldAnnotation, annotation != null);
        if (StringUtils.isEmpty(filedLogName)) {
            return;
        }
        // 是否是容器类型的字段
        boolean valueIsContainer = valueIsContainer(node, sourceObject, targetObject);
        String functionName = diffLogFieldAnnotation != null ? diffLogFieldAnnotation.function() : "";
        String logContent = valueIsContainer
                ? getCollectionDiffLogContent(filedLogName, node, sourceObject, targetObject, functionName)
                : getDiffLogContent(filedLogName, node, sourceObject, targetObject, functionName);
        if (!StringUtils.isEmpty(logContent)) {
            stringBuilder.append(logContent).append(logRecordProperties.getFieldSeparator());
        }
        memorandum(node, set);
    }

    /**
     * 将节点及其子节点登记为已处理，避免被外层遍历重复处理。
     *
     * @param node 差异节点
     * @param set  已处理节点集合
     */
    private void memorandum(DiffNode node, Set<DiffNode> set) {
        set.add(node);
        if (node.hasChildren()) {
            Field childrenField = ReflectionUtils.findField(DiffNode.class, "children");
            assert childrenField != null;
            ReflectionUtils.makeAccessible(childrenField);
            Map<ElementSelector, DiffNode> children = (Map<ElementSelector, DiffNode>) ReflectionUtils.getField(childrenField, node);
            assert children != null;
            for (DiffNode value : children.values()) memorandum(value, set);
        }
    }

    /**
     * 取字段在日志中显示的名称，含父级字段路径拼接。
     *
     * @param node                  差异节点
     * @param diffLogFieldAnnotation 字段注解（可能为空）
     * @param isField               是否启用属性名映射
     * @return 字段显示名
     */
    private String getFieldLogName(DiffNode node, DiffLogField diffLogFieldAnnotation, boolean isField) {
        String filedLogName = diffLogFieldAnnotation != null ? diffLogFieldAnnotation.name() : node.getPropertyName();
        if (node.getParentNode() != null) {
            //获取对象的定语：比如：创建人的ID
            filedLogName = getParentFieldName(node, isField) + filedLogName;
        }
        return filedLogName;
    }

    /**
     * 判断节点取值是否为容器类型（集合或数组）。
     *
     * @param node         差异节点
     * @param sourceObject 旧对象
     * @param targetObject 新对象
     * @return 容器类型返回 {@code true}
     */
    private boolean valueIsContainer(DiffNode node, Object sourceObject, Object targetObject) {
        if (sourceObject != null) {
            Object sourceValue = node.canonicalGet(sourceObject);
            if (sourceValue == null) {
                if (targetObject != null) {
                    return node.canonicalGet(targetObject) instanceof Collection || node.canonicalGet(targetObject).getClass().isArray();
                }
            } else {
                return sourceValue instanceof Collection || sourceValue.getClass().isArray();
            }
        }
        return false;
    }

    /**
     * 拼接父级字段名前缀（嵌套对象路径），以 {@code ofWord} 连接。
     *
     * @param node    差异节点
     * @param isField 是否启用属性名映射
     * @return 父级字段名前缀
     */
    private String getParentFieldName(DiffNode node, boolean isField) {
        DiffNode parent = node.getParentNode();
        String fieldNamePrefix = "";
        while (parent != null) {
            DiffLogField diffLogFieldAnnotation = parent.getFieldAnnotation(DiffLogField.class);
            if ((diffLogFieldAnnotation == null && !isField) || parent.isRootNode()) {
                // 父节点没有配置名称且不用属性名映射，不拼接
                parent = parent.getParentNode();
                continue;
            }
            fieldNamePrefix = diffLogFieldAnnotation != null
                    ? diffLogFieldAnnotation.name().concat(logRecordProperties.getOfWord()).concat(fieldNamePrefix)
                    : parent.getPropertyName().concat(logRecordProperties.getOfWord()).concat(fieldNamePrefix);
            parent = parent.getParentNode();
        }
        return fieldNamePrefix;
    }

    /**
     * 生成集合/数组字段的差异文案（按添加/删除项分别渲染）。
     *
     * @param filedLogName 字段显示名
     * @param node         差异节点
     * @param sourceObject 旧对象
     * @param targetObject 新对象
     * @param functionName 值转换函数名
     * @return 集合差异文案
     */
    public String getCollectionDiffLogContent(String filedLogName, DiffNode node, Object sourceObject, Object targetObject, String functionName) {
        //集合走单独的diff模板
        Collection<Object> sourceList = getListValue(node, sourceObject);
        Collection<Object> targetList = getListValue(node, targetObject);
        Collection<Object> addItemList = listSubtract(targetList, sourceList);
        Collection<Object> delItemList = listSubtract(sourceList, targetList);
        String listAddContent = listToContent(functionName, addItemList);
        String listDelContent = listToContent(functionName, delItemList);
        return logRecordProperties.formatList(filedLogName, listAddContent, listDelContent);
    }

    /**
     * 按节点状态（新增/变更/删除）生成普通字段的差异文案。
     *
     * @param filedLogName 字段显示名
     * @param node         差异节点
     * @param sourceObject 旧对象
     * @param targetObject 新对象
     * @param functionName 值转换函数名
     * @return 字段差异文案
     */
    public String getDiffLogContent(String filedLogName, DiffNode node, Object sourceObject, Object targetObject, String functionName) {
        switch (node.getState()) {
            case ADDED:
                return logRecordProperties.formatAdd(filedLogName, getFunctionValue(getFieldValue(node, targetObject), functionName));
            case CHANGED:
                return logRecordProperties.formatUpdate(filedLogName, getFunctionValue(getFieldValue(node, sourceObject), functionName), getFunctionValue(getFieldValue(node, targetObject), functionName));
            case REMOVED:
                return logRecordProperties.formatDeleted(filedLogName, getFunctionValue(getFieldValue(node, sourceObject), functionName));
            default:
                log.warn("diff log not support");
                return "";
        }
    }

    /**
     * 取节点对应字段的集合值（数组统一转为 List）。
     *
     * @param node   差异节点
     * @param object 目标对象
     * @return 集合值
     */
    private Collection<Object> getListValue(DiffNode node, Object object) {
        Object fieldSourceValue = getFieldValue(node, object);
        //noinspection unchecked
        if (fieldSourceValue != null && fieldSourceValue.getClass().isArray()) {
            return new ArrayList<>(Arrays.asList((Object[]) fieldSourceValue));
        }
        return fieldSourceValue == null ? new ArrayList<>() : (Collection<Object>) fieldSourceValue;
    }

    /**
     * 集合差集：返回 {@code minuend} 中存在而 {@code subTractor} 中不存在的元素。
     *
     * @param minuend   被减集合
     * @param subTractor 减数集合
     * @return 差集
     */
    private Collection<Object> listSubtract(Collection<Object> minuend, Collection<Object> subTractor) {
        Collection<Object> addItemList = new ArrayList<>(minuend);
        addItemList.removeAll(subTractor);
        return addItemList;
    }

    /**
     * 将集合元素转为文案，元素间以列表项分隔符拼接。
     *
     * @param functionName 值转换函数名
     * @param addItemList  元素集合
     * @return 拼接后的文案
     */
    private String listToContent(String functionName, Collection<Object> addItemList) {
        StringBuilder listAddContent = new StringBuilder();
        if (!CollectionUtils.isEmpty(addItemList)) {
            for (Object item : addItemList) {
                listAddContent.append(getFunctionValue(item, functionName)).append(logRecordProperties.getListItemSeparator());
            }
        }
        return listAddContent.toString().replaceAll(logRecordProperties.getListItemSeparator() + "$", "");
    }

    /**
     * 对字段值应用转换函数；未配置函数时直接取字符串形式。
     *
     * @param canonicalGet  字段值
     * @param functionName  值转换函数名
     * @return 转换后的文案
     */
    private String getFunctionValue(Object canonicalGet, String functionName) {
        if (StringUtils.isEmpty(functionName)) {
            return canonicalGet.toString();
        }
        return functionService.apply(functionName, canonicalGet.toString());
    }

    /**
     * 取节点在目标对象上的字段值。
     *
     * @param node 差异节点
     * @param o2   目标对象
     * @return 字段值
     */
    private Object getFieldValue(DiffNode node, Object o2) {
        return node.canonicalGet(o2);
    }

    /**
     * 注入 Bean 工厂。
     *
     * @param beanFactory Bean 工厂
     * @throws BeansException 异常
     */
    @Override
    public void setBeanFactory(@NonNull BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    /**
     * 单例就绪后从容器获取函数服务。
     */
    @Override
    public void afterSingletonsInstantiated() {
        this.functionService = beanFactory.getBean(IFunctionService.class);
    }
}
