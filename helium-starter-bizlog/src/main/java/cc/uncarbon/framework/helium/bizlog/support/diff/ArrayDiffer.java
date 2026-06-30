package cc.uncarbon.framework.helium.bizlog.support.diff;

import cc.uncarbon.framework.helium.bizlog.service.impl.DiffParseFunction;
import de.danielbechler.diff.access.Accessor;
import de.danielbechler.diff.access.Instances;
import de.danielbechler.diff.comparison.ComparisonStrategy;
import de.danielbechler.diff.comparison.ComparisonStrategyResolver;
import de.danielbechler.diff.differ.Differ;
import de.danielbechler.diff.differ.DifferDispatcher;
import de.danielbechler.diff.identity.IdentityStrategy;
import de.danielbechler.diff.identity.IdentityStrategyResolver;
import de.danielbechler.diff.node.DiffNode;
import de.danielbechler.util.Assert;

import java.util.*;

/**
 * 数组差异比较器
 * <p>
 * 为 {@code java-object-diff} 补充对数组类型的逐元素差异比较能力，
 * 由 {@link DiffParseFunction} 注册到差异引擎。
 *
 * @author wulang@mzt-biz-log
 * @author Uncarbon
 **/
public class ArrayDiffer implements Differ {
    private final DifferDispatcher differDispatcher;
    private final ComparisonStrategyResolver comparisonStrategyResolver;
    private final IdentityStrategyResolver identityStrategyResolver;

    /**
     * 注入差异派发器、比较策略与一致性策略解析器。
     *
     * @param differDispatcher            差异派发器
     * @param comparisonStrategyResolver  比较策略解析器
     * @param identityStrategyResolver    一致性策略解析器
     */
    public ArrayDiffer(DifferDispatcher differDispatcher, ComparisonStrategyResolver comparisonStrategyResolver, IdentityStrategyResolver identityStrategyResolver) {
        Assert.notNull(differDispatcher, "differDispatcher");
        this.differDispatcher = differDispatcher;
        Assert.notNull(comparisonStrategyResolver, "comparisonStrategyResolver");
        this.comparisonStrategyResolver = comparisonStrategyResolver;
        Assert.notNull(identityStrategyResolver, "identityStrategyResolver");
        this.identityStrategyResolver = identityStrategyResolver;
    }

    /**
     * 是否处理非基本类型的数组。
     *
     * @param type 待判断类型
     * @return 非基本类型数组返回 {@code true}
     */
    @Override
    public boolean accepts(final Class<?> type) {
        return !type.isPrimitive() && type.isArray();
    }

    /**
     * 比较数组实例，按新增/删除/未变更/变更分别处理，生成差异节点。
     *
     * @param parentNode          父差异节点
     * @param collectionInstances 待比较的数组实例
     * @return 数组差异节点
     */
    @Override
    public final DiffNode compare(final DiffNode parentNode, final Instances collectionInstances) {
        final DiffNode collectionNode = newNode(parentNode, collectionInstances);
        final IdentityStrategy identityStrategy = identityStrategyResolver.resolveIdentityStrategy(collectionNode);
        if (identityStrategy != null) {
            collectionNode.setChildIdentityStrategy(identityStrategy);
        }
        if (collectionInstances.hasBeenAdded()) {
            final Collection<?> addedItems = findCollection(collectionInstances.getWorking());
            compareItems(collectionNode, collectionInstances, addedItems, identityStrategy);
            collectionNode.setState(DiffNode.State.ADDED);
        } else if (collectionInstances.hasBeenRemoved()) {
            final Collection<?> removedItems = findCollection(collectionInstances.getBase());
            compareItems(collectionNode, collectionInstances, removedItems, identityStrategy);
            collectionNode.setState(DiffNode.State.REMOVED);
        } else if (collectionInstances.areSame()) {
            collectionNode.setState(DiffNode.State.UNTOUCHED);
        } else {
            final ComparisonStrategy comparisonStrategy = comparisonStrategyResolver.resolveComparisonStrategy(collectionNode);
            if (comparisonStrategy == null) {
                compareInternally(collectionNode, collectionInstances, identityStrategy);
            } else {
                compareUsingComparisonStrategy(collectionNode, collectionInstances, comparisonStrategy);
            }
        }
        return collectionNode;
    }

    /**
     * 将数组转为集合（{@code null} 转为空集合）。
     *
     * @param source 数组对象
     * @return 集合
     */
    private Collection<?> findCollection(Object source) {
        return source == null ? new ArrayList<>() : new LinkedList<>(Arrays.asList((Object[]) source));
    }

    /**
     * 创建数组差异节点。
     *
     * @param parentNode          父节点
     * @param collectionInstances 数组实例
     * @return 差异节点
     */
    private static DiffNode newNode(final DiffNode parentNode,
                                    final Instances collectionInstances) {
        final Accessor accessor = collectionInstances.getSourceAccessor();
        final Class<?> type = collectionInstances.getType();
        return new DiffNode(parentNode, accessor, type);
    }

    /**
     * 逐元素派发比较。
     *
     * @param collectionNode    数组差异节点
     * @param collectionInstances 数组实例
     * @param items             待比较元素
     * @param identityStrategy  一致性策略
     */
    private void compareItems(final DiffNode collectionNode,
                              final Instances collectionInstances,
                              final Iterable<?> items,
                              final IdentityStrategy identityStrategy) {
        for (final Object item : items) {
            final Accessor itemAccessor = new ArrayItemAccessor(item, identityStrategy);
            differDispatcher.dispatch(collectionNode, collectionInstances, itemAccessor);
        }
    }

    /**
     * 内部比较：计算新增、删除、已知元素并逐项派发比较。
     *
     * @param collectionNode     数组差异节点
     * @param collectionInstances 数组实例
     * @param identityStrategy   一致性策略
     */
    private void compareInternally(final DiffNode collectionNode,
                                   final Instances collectionInstances,
                                   final IdentityStrategy identityStrategy) {
        final Collection<?> working = Arrays.asList((Object[]) collectionInstances.getWorking());
        final Collection<?> base = Arrays.asList((Object[]) collectionInstances.getBase());

        final Iterable<?> added = new LinkedList<Object>(working);
        final Iterable<?> removed = new LinkedList<Object>(base);
        final Iterable<?> known = new LinkedList<Object>(base);

        remove(added, base, identityStrategy);
        remove(removed, working, identityStrategy);
        remove(known, added, identityStrategy);
        remove(known, removed, identityStrategy);

        compareItems(collectionNode, collectionInstances, added, identityStrategy);
        compareItems(collectionNode, collectionInstances, removed, identityStrategy);
        compareItems(collectionNode, collectionInstances, known, identityStrategy);
    }

    /**
     * 使用自定义比较策略进行比较。
     *
     * @param collectionNode      数组差异节点
     * @param collectionInstances 数组实例
     * @param comparisonStrategy  比较策略
     */
    private static void compareUsingComparisonStrategy(final DiffNode collectionNode,
                                                       final Instances collectionInstances,
                                                       final ComparisonStrategy comparisonStrategy) {
        comparisonStrategy.compare(collectionNode,
                collectionInstances.getType(),
                collectionInstances.getWorking(Collection.class),
                collectionInstances.getBase(Collection.class));
    }

    /**
     * 从 {@code from} 中移除同时存在于 {@code these} 的元素。
     *
     * @param from             源集合
     * @param these            对照集合
     * @param identityStrategy 一致性策略
     */
    private void remove(final Iterable<?> from, final Iterable<?> these, final IdentityStrategy identityStrategy) {
        final Iterator<?> iterator = from.iterator();
        while (iterator.hasNext()) {
            final Object item = iterator.next();
            if (contains(these, item, identityStrategy)) {
                iterator.remove();
            }
        }
    }

    /**
     * 判断 {@code haystack} 中是否包含与 {@code needle} 一致的元素。
     *
     * @param haystack         被查找集合
     * @param needle           目标元素
     * @param identityStrategy 一致性策略
     * @return 包含返回 {@code true}
     */
    private boolean contains(final Iterable<?> haystack, final Object needle, final IdentityStrategy identityStrategy) {
        for (final Object item : haystack) {
            if (identityStrategy.equals(needle, item)) {
                return true;
            }
        }
        return false;
    }
}
