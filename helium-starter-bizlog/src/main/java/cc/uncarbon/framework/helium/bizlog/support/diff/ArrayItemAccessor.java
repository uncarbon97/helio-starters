package cc.uncarbon.framework.helium.bizlog.support.diff;

import de.danielbechler.diff.access.Accessor;
import de.danielbechler.diff.access.TypeAwareAccessor;
import de.danielbechler.diff.identity.EqualsIdentityStrategy;
import de.danielbechler.diff.identity.IdentityStrategy;
import de.danielbechler.diff.selector.CollectionItemElementSelector;
import de.danielbechler.diff.selector.ElementSelector;
import org.springframework.util.Assert;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 数组元素访问器
 * <p>
 * 将数组元素以集合元素的方式暴露给 {@code java-object-diff}，
 * 配合 {@link ArrayDiffer} 完成数组元素的只读 get；数组定长，set/unset 不支持。
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
        Assert.notNull(identityStrategy, "identityStrategy 不能为空");
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
        final Collection<Object> targetCollection = objectAsCollection(target);
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
     * 数组定长，无法原地增删元素；本访问器仅服务只读 diff，不支持写回
     *
     * @throws UnsupportedOperationException 一律抛出
     */
    @Override
    public void set(final Object target, final Object value) {
        throw new UnsupportedOperationException("数组元素访问器仅支持只读 diff，不支持 set 写回");
    }

    /**
     * 将数组对象转为集合视图（只读快照；基本类型数组按元素装箱）。
     *
     * @param object 数组对象
     * @return 集合视图；{@code null} 返回 {@code null}，非数组抛出非法参数异常
     */
    private static Collection<Object> objectAsCollection(final Object object) {
        if (object == null) {
            return null;
        } else if (object.getClass().isArray()) {
            int length = Array.getLength(object);
            List<Object> ret = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                ret.add(Array.get(object, i));
            }
            return ret;
        }
        throw new IllegalArgumentException(object.getClass().toString());
    }

    /**
     * 数组定长，无法原地增删元素；本访问器仅服务只读 diff，不支持写回
     *
     * @throws UnsupportedOperationException 一律抛出
     */
    @Override
    public void unset(final Object target) {
        throw new UnsupportedOperationException("数组元素访问器仅支持只读 diff，不支持 unset 写回");
    }
}
