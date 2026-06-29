package cc.uncarbon.framework.helium.bizlog.diff;

import de.danielbechler.diff.access.Accessor;
import de.danielbechler.diff.access.TypeAwareAccessor;
import de.danielbechler.diff.identity.EqualsIdentityStrategy;
import de.danielbechler.diff.identity.IdentityStrategy;
import de.danielbechler.diff.selector.CollectionItemElementSelector;
import de.danielbechler.diff.selector.ElementSelector;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * 数组元素访问器
 * <p>
 * 将数组元素以集合元素的方式暴露给 {@code java-object-diff}，
 * 配合 {@link ArrayDiffer} 完成数组元素的 get/set/unset。
 *
 * @author wulang@mzt-biz-log
 * @author Uncarbon
 **/
public class ArrayItemAccessor implements TypeAwareAccessor, Accessor {

    private final Object referenceItem;
    private final IdentityStrategy identityStrategy;

    /**
     * 以默认一致性策略构造访问器。
     *
     * @param referenceItem 参考元素
     */
    public ArrayItemAccessor(final Object referenceItem) {
        this(referenceItem, EqualsIdentityStrategy.getInstance());
    }

    /**
     * 指定一致性策略构造访问器。
     *
     * @param referenceItem    参考元素
     * @param identityStrategy 一致性策略
     */
    public ArrayItemAccessor(final Object referenceItem,
                             final IdentityStrategy identityStrategy) {
        Assert.notNull(identityStrategy, "identityStrategy");
        this.referenceItem = referenceItem;
        this.identityStrategy = identityStrategy;
    }

    /**
     * 取参考元素的类型。
     *
     * @return 元素类型，参考元素为 {@code null} 时返回 {@code null}
     */
    @Override
    public Class<?> getType() {
        return referenceItem != null ? referenceItem.getClass() : null;
    }

    /**
     * @return 元素选择器描述
     */
    @Override
    public String toString() {
        return "collection item " + getElementSelector();
    }

    /**
     * 取元素选择器。
     *
     * @return 元素选择器
     */
    @Override
    public ElementSelector getElementSelector() {
        final CollectionItemElementSelector selector = new CollectionItemElementSelector(referenceItem);
        return identityStrategy == null ? selector : selector.copyWithIdentityStrategy(identityStrategy);
    }

    /**
     * 从目标数组中查找与参考元素一致的元素。
     *
     * @param target 目标数组
     * @return 命中元素，未命中或目标为空返回 {@code null}
     */
    @Override
    public Object get(final Object target) {
        final Collection targetCollection = objectAsCollection(target);
        if (targetCollection == null) {
            return null;
        }
        for (final Object item : targetCollection) {
            if (item != null && identityStrategy.equals(item, referenceItem)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 设置数组元素：若已存在相同元素则先移除再添加。
     *
     * @param target 目标数组
     * @param value  新值
     */
    @Override
    public void set(final Object target, final Object value) {
        final Collection<Object> targetCollection = objectAsCollection(target);
        if (targetCollection == null) {
            return;
        }
        final Object previous = get(target);
        if (previous != null) {
            unset(target);
        }
        targetCollection.add(value);
    }

    /**
     * 将数组对象转为集合视图。
     *
     * @param object 数组对象
     * @return 集合视图；{@code null} 返回 {@code null}，非数组抛出非法参数异常
     */
    @SuppressWarnings("unchecked")
    private static Collection<Object> objectAsCollection(final Object object) {
        if (object == null) {
            return null;
        } else if (object.getClass().isArray()) {
            return new ArrayList<>(Arrays.asList((Object[]) object));
        }
        throw new IllegalArgumentException(object.getClass().toString());
    }

    /**
     * 从目标数组中移除与参考元素一致的元素。
     *
     * @param target 目标数组
     */
    @Override
    public void unset(final Object target) {
        final Collection<?> targetCollection = objectAsCollection(target);
        if (targetCollection == null) {
            return;
        }
        final Iterator<?> iterator = targetCollection.iterator();
        while (iterator.hasNext()) {
            final Object item = iterator.next();
            if (item != null && identityStrategy.equals(item, referenceItem)) {
                iterator.remove();
                break;
            }
        }
    }
}
