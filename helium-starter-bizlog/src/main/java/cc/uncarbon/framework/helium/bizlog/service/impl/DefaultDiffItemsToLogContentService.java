package cc.uncarbon.framework.helium.bizlog.service.impl;

import cc.uncarbon.framework.helium.bizlog.annotation.DiffLogAllFields;
import cc.uncarbon.framework.helium.bizlog.annotation.DiffLogField;
import cc.uncarbon.framework.helium.bizlog.annotation.DiffLogIgnore;
import cc.uncarbon.framework.helium.bizlog.props.HeliumBizLogProperties;
import cc.uncarbon.framework.helium.bizlog.service.IDiffItemsToLogContentService;
import cc.uncarbon.framework.helium.bizlog.service.IFunctionService;
import cc.uncarbon.framework.helium.bizlog.support.diff.DiffTextFormatter;
import de.danielbechler.diff.node.DiffNode;
import de.danielbechler.diff.selector.ElementSelector;
import de.danielbechler.diff.selector.MapKeyElementSelector;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 差异节点转文案默认实现
 * <p>
 * 遍历 {@link DiffNode} 差异树，按字段配置（{@link DiffLogField} / {@link DiffLogAllFields} / {@link DiffLogIgnore}）
 * 生成形如「字段名从【旧值】修改为【新值】」的日志文案，支持嵌套对象与集合。
 *
 * @author muzhantong@mzt-biz-log
 * @author Uncarbon
 */
@Setter
@Getter
@RequiredArgsConstructor
@Slf4j
public class DefaultDiffItemsToLogContentService implements IDiffItemsToLogContentService, BeanFactoryAware, SmartInitializingSingleton {

    private final HeliumBizLogProperties props;
    private IFunctionService functionService;
    private BeanFactory beanFactory;

    /**
     * 复杂值递归脱敏渲染的最大层级，防御超深嵌套
     */
    private static final int MAX_MASKED_VALUE_DEPTH = 8;

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
        diffNode.visit((node, _) -> generateAllFieldLog(sourceObject, targetObject, stringBuilder, node, annotation, set));
        set.clear();
        // 分隔符是配置项，可能含正则元字符，须 Pattern.quote 按字面匹配
        return stringBuilder.toString().replaceAll(Pattern.quote(props.getFieldSeparator()) + "$", "");
    }

    /**
     * 针对单个差异节点生成字段级文案，并追加到结果中。
     *
     * @param sourceObject  旧对象
     * @param targetObject  新对象
     * @param stringBuilder 文案构建器
     * @param node          差异节点
     * @param annotation    类级 {@link DiffLogAllFields}（可能为空）
     * @param set           已处理节点集合（防重复）
     */
    private void generateAllFieldLog(Object sourceObject, Object targetObject, StringBuilder stringBuilder, DiffNode node,
                                     DiffLogAllFields annotation, Set<DiffNode> set) {
        if (node.isRootNode() || node.getValueTypeInfo() != null || set.contains(node)) {
            return;
        }
        DiffLogIgnore logIgnore = node.getFieldAnnotation(DiffLogIgnore.class);
        if (logIgnore != null) {
            memorandum(node, set);
            return;
        }
        DiffLogField diffLogField = node.getFieldAnnotation(DiffLogField.class);
        if (annotation == null && diffLogField == null) {
            return;
        }
        String filedLogName = getFieldLogName(node, diffLogField, annotation != null);
        if (ObjectUtils.isEmpty(filedLogName)) {
            return;
        }
        if (isIgnoredDiffField(node)) {
            // 命中脱敏清单：不输出旧/新值，仅以 *** 占位记录字段已变更，避免敏感信息明文入库
            stringBuilder.append("【").append(filedLogName).append("】").append(props.getIgnoredDiffFieldMask()).append(props.getFieldSeparator());
            memorandum(node, set);
            return;
        }
        // 是否是容器类型的字段
        boolean valueIsContainer = valueIsContainer(node, sourceObject, targetObject);
        String functionName = diffLogField != null ? diffLogField.function() : "";
        String logContent = valueIsContainer
                ? getCollectionDiffLogContent(filedLogName, node, sourceObject, targetObject, functionName)
                : getDiffLogContent(filedLogName, node, sourceObject, targetObject, functionName);
        if (!ObjectUtils.isEmpty(logContent)) {
            stringBuilder.append(logContent).append(props.getFieldSeparator());
        }
        memorandum(node, set);
    }

    /**
     * 是否简单值类型（无内部结构，无需按字段脱敏）。
     *
     * @param clazz 待判断类型
     * @return 简单类型返回 {@code true}
     */
    private static boolean isSimpleValueType(Class<?> clazz) {
        return clazz.isPrimitive()
                || clazz.isEnum()
                || CharSequence.class.isAssignableFrom(clazz)
                || Number.class.isAssignableFrom(clazz)
                || Boolean.class == clazz
                || Character.class == clazz
                || Date.class.isAssignableFrom(clazz)
                || Temporal.class.isAssignableFrom(clazz)
                || clazz == UUID.class
                || clazz == Class.class;
    }

    /**
     * 字段名是否命中脱敏清单（不区分大小写，按属性名而非显示名匹配）。
     */
    private boolean isIgnoredDiffField(DiffNode node) {
        return isIgnoredFieldName(node.getPropertyName()) || isIgnoredMapEntryNode(node);
    }

    /**
     * 名称是否命中脱敏清单（不区分大小写，适用于属性名 / 字段名 / Map 键名）。
     *
     * @param fieldName 待检查名称，可为 null
     * @return 命中返回 {@code true}
     */
    private boolean isIgnoredFieldName(String fieldName) {
        if (fieldName == null) {
            return false;
        }
        List<String> ignoredDiffFields = props.getIgnoredDiffFields();
        if (CollectionUtils.isEmpty(ignoredDiffFields)) {
            return false;
        }
        for (String ignoredName : ignoredDiffFields) {
            if (fieldName.equalsIgnoreCase(ignoredName)) {
                return true;
            }
        }
        return false;
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
     * @param node         差异节点
     * @param diffLogField 字段注解（可能为空）
     * @param isField      是否启用属性名映射
     * @return 字段显示名
     */
    private String getFieldLogName(DiffNode node, DiffLogField diffLogField, boolean isField) {
        String filedLogName = getCurrentFieldLogName(node, diffLogField);
        if (node.getParentNode() != null) {
            //获取对象的定语：比如：创建人的ID
            filedLogName = getParentFieldName(node, isField) + filedLogName;
        }
        return filedLogName;
    }

    /**
     * 取当前字段显示名：优先使用 {@link DiffLogField#name()}，其次使用 {@link Schema#description()}，最后使用属性名。
     *
     * @param node         差异节点
     * @param diffLogField 字段注解（可能为空）
     * @return 当前字段显示名
     */
    private String getCurrentFieldLogName(DiffNode node, DiffLogField diffLogField) {
        if (diffLogField != null) {
            return diffLogField.name();
        }
        Schema schemaAnnotation = node.getFieldAnnotation(Schema.class);
        if (schemaAnnotation != null && !ObjectUtils.isEmpty(schemaAnnotation.description())) {
            return schemaAnnotation.description();
        }
        return node.getPropertyName();
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
            fieldNamePrefix = getCurrentFieldLogName(parent, diffLogFieldAnnotation).concat(props.getOfWord()).concat(fieldNamePrefix);
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
        return DiffTextFormatter.formatList(props, filedLogName, listAddContent, listDelContent);
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
        return switch (node.getState()) {
            case ADDED ->
                    DiffTextFormatter.formatAdd(props, filedLogName, getFunctionValue(getFieldValue(node, targetObject), functionName));
            case CHANGED ->
                    DiffTextFormatter.formatUpdate(props, filedLogName, getFunctionValue(getFieldValue(node, sourceObject), functionName), getFunctionValue(getFieldValue(node, targetObject), functionName));
            case REMOVED ->
                    DiffTextFormatter.formatDeleted(props, filedLogName, getFunctionValue(getFieldValue(node, sourceObject), functionName));
            default -> {
                log.warn("diff log not support");
                yield "";
            }
        };
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
        if (fieldSourceValue != null && fieldSourceValue.getClass().isArray()) {
            Class<?> componentType = fieldSourceValue.getClass().getComponentType();
            if (componentType.isPrimitive()) {
                // 基本类型数组不能强转 Object[]，按 Array.get 装箱，避免 CCE 导致整条日志丢失
                int length = Array.getLength(fieldSourceValue);
                List<Object> boxed = new ArrayList<>(length);
                for (int i = 0; i < length; i++) {
                    boxed.add(Array.get(fieldSourceValue, i));
                }
                return boxed;
            }
            //noinspection unchecked
            return new ArrayList<>(Arrays.asList((Object[]) fieldSourceValue));
        }
        return fieldSourceValue == null ? new ArrayList<>() : (Collection<Object>) fieldSourceValue;
    }

    /**
     * 集合差集：返回 {@code minuend} 中存在而 {@code subTractor} 中不存在的元素。
     * <p>元素类未重写 {@code equals} 时回退 {@code toString} 比对，
     * 避免未变化元素同时出现在「添加」与「删除」文案中。</p>
     *
     * @param minuend    被减集合
     * @param subTractor 减数集合
     * @return 差集
     */
    private Collection<Object> listSubtract(Collection<Object> minuend, Collection<Object> subTractor) {
        Collection<Object> addItemList = new ArrayList<>(minuend);
        if (usesDefaultEquals(minuend) || usesDefaultEquals(subTractor)) {
            Set<String> subTractorStrings = subTractor.stream().map(String::valueOf).collect(Collectors.toSet());
            addItemList.removeIf(item -> subTractorStrings.contains(String.valueOf(item)));
            return addItemList;
        }
        addItemList.removeAll(subTractor);
        return addItemList;
    }

    /**
     * 集合中是否存在未重写 {@code equals} 的元素类（null 元素忽略）。
     */
    private boolean usesDefaultEquals(Collection<?> collection) {
        for (Object item : collection) {
            if (item != null && !isEqualsOverridden(item.getClass())) {
                return true;
            }
        }
        return false;
    }

    private boolean isEqualsOverridden(Class<?> clazz) {
        try {
            return clazz.getMethod("equals", Object.class).getDeclaringClass() != Object.class;
        } catch (NoSuchMethodException e) {
            return false;
        }
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
                listAddContent.append(getFunctionValue(item, functionName)).append(props.getListItemSeparator());
            }
        }
        return listAddContent.toString().replaceAll(Pattern.quote(props.getListItemSeparator()) + "$", "");
    }

    /**
     * Map 值节点的键名是否命中脱敏清单（Map 值节点的属性名会回退为 Map 字段名，需按键名单独判断）。
     *
     * @param node 差异节点
     * @return 是 Map 键值对节点且键名命中返回 {@code true}
     */
    private boolean isIgnoredMapEntryNode(DiffNode node) {
        if (node.getParentNode() == null) {
            return false;
        }
        ElementSelector selector = node.getPath().getLastElementSelector();
        if (selector instanceof MapKeyElementSelector keySelector) {
            Object key = keySelector.getKey();
            return key != null && isIgnoredFieldName(String.valueOf(key));
        }
        return false;
    }

    /**
     * 对字段值应用转换函数；未配置函数时直接取字符串形式。
     *
     * @param canonicalGet 字段值
     * @param functionName 值转换函数名
     * @return 转换后的文案
     */
    private String getFunctionValue(Object canonicalGet, String functionName) {
        // 集合/字段值可能为 null，判空避免丢整条 diff 日志
        if (canonicalGet == null) {
            return "";
        }
        if (ObjectUtils.isEmpty(functionName)) {
            // 未配置转换函数：复杂值（如集合元素 DTO）须递归脱敏后再渲染，防止敏感字段经 toString 明文外泄
            return toDisplayString(canonicalGet);
        }
        // 配置了转换函数：渲染逻辑由函数负责，函数侧需自行处理敏感信息
        return functionService.apply(functionName, canonicalGet.toString());
    }

    /**
     * 值转展示文案：简单类型直接 toString；
     * 复杂对象（集合元素 DTO、equals 整体比较的 Bean 等）按字段名递归匹配脱敏清单，
     * 命中字段值替换为 {@code ignoredDiffFieldMask}；未命中任何字段时回退原始 toString，保持原格式不变。
     *
     * @param value 待渲染值
     * @return 展示文案
     */
    private String toDisplayString(Object value) {
        if (CollectionUtils.isEmpty(props.getIgnoredDiffFields()) || isSimpleValueType(value.getClass())) {
            return value.toString();
        }
        StringBuilder stringBuilder = new StringBuilder();
        boolean masked = appendMaskedValue(value, stringBuilder, Collections.newSetFromMap(new IdentityHashMap<>()), 0);
        return masked ? stringBuilder.toString() : value.toString();
    }

    /**
     * 递归渲染复杂值：数组 / 集合 / Map / Bean 按字段名（Map 按键名）匹配脱敏清单，
     * 命中的值替换为掩码文案。
     *
     * @param value         待渲染值
     * @param stringBuilder 文案构建器
     * @param visiting      当前渲染路径上的对象集合，防自引用死循环
     * @param depth         当前递归层级
     * @return 是否发生了脱敏替换
     */
    private boolean appendMaskedValue(Object value, StringBuilder stringBuilder, Set<Object> visiting, int depth) {
        if (value == null) {
            stringBuilder.append("null");
            return false;
        }
        Class<?> clazz = value.getClass();
        if (isSimpleValueType(clazz)) {
            stringBuilder.append(value);
            return false;
        }
        if (depth >= MAX_MASKED_VALUE_DEPTH) {
            stringBuilder.append("…");
            return false;
        }
        if (!visiting.add(value)) {
            stringBuilder.append("(循环引用)");
            return false;
        }
        boolean masked = false;
        if (clazz.isArray()) {
            stringBuilder.append('[');
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                if (i > 0) {
                    stringBuilder.append(", ");
                }
                masked |= appendMaskedValue(Array.get(value, i), stringBuilder, visiting, depth + 1);
            }
            stringBuilder.append(']');
        } else if (value instanceof Map<?, ?> map) {
            stringBuilder.append('{');
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) {
                    stringBuilder.append(", ");
                }
                first = false;
                stringBuilder.append(entry.getKey()).append('=');
                if (isIgnoredFieldName(String.valueOf(entry.getKey()))) {
                    stringBuilder.append(props.getIgnoredDiffFieldMask());
                    masked = true;
                } else {
                    masked |= appendMaskedValue(entry.getValue(), stringBuilder, visiting, depth + 1);
                }
            }
            stringBuilder.append('}');
        } else if (value instanceof Collection<?> collection) {
            stringBuilder.append('[');
            boolean first = true;
            for (Object item : collection) {
                if (!first) {
                    stringBuilder.append(", ");
                }
                first = false;
                masked |= appendMaskedValue(item, stringBuilder, visiting, depth + 1);
            }
            stringBuilder.append(']');
        } else {
            // Bean：按声明字段名匹配脱敏清单
            stringBuilder.append(clazz.getSimpleName()).append('(');
            boolean first = true;
            for (Class<?> current = clazz; current != null && current != Object.class; current = current.getSuperclass()) {
                for (Field field : current.getDeclaredFields()) {
                    if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                        continue;
                    }
                    if (!first) {
                        stringBuilder.append(", ");
                    }
                    first = false;
                    stringBuilder.append(field.getName()).append('=');
                    if (isIgnoredFieldName(field.getName())) {
                        stringBuilder.append(props.getIgnoredDiffFieldMask());
                        masked = true;
                        continue;
                    }
                    Object fieldValue;
                    try {
                        ReflectionUtils.makeAccessible(field);
                        fieldValue = field.get(value);
                    } catch (IllegalAccessException e) {
                        stringBuilder.append('?');
                        continue;
                    }
                    masked |= appendMaskedValue(fieldValue, stringBuilder, visiting, depth + 1);
                }
            }
            stringBuilder.append(')');
        }
        // 出栈后移除，允许同一对象出现在兄弟分支
        visiting.remove(value);
        return masked;
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
