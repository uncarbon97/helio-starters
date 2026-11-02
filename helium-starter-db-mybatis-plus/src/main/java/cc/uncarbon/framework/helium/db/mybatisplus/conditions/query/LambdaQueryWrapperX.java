package cc.uncarbon.framework.helium.db.mybatisplus.conditions.query;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * 拓展 MyBatis Plus QueryWrapper 类，主要增加如下功能：
 * <p>
 * 1. 拼接条件的方法，增加 xxxIfPresent 方法，用于判断值不存在的时候，不要拼接到条件中。
 *
 * @param <T> 数据类型
 * @author yudao
 */
public class LambdaQueryWrapperX<T> extends LambdaQueryWrapper<T> {

    /**
     * 模糊查询入参是否转义 LIKE 通配符（{@code \ % _}）；
     * 默认转义，避免用户输入 {@code %} 退化为全表扫 / 信息泄漏。
     * 转义字符为反斜杠，与 MySQL / PostgreSQL 的默认 LIKE 转义口径一致；其他方言请自行追加 ESCAPE 子句
     */
    private boolean escapeLikeWildcards = true;

    public LambdaQueryWrapperX<T> escapeLikeWildcards(boolean escape) {
        this.escapeLikeWildcards = escape;
        return this;
    }

    /**
     * 转义 LIKE 通配符：{@code \ -> \\}, {@code % -> \%}, {@code _ -> \_}
     */
    public static String escapeLike(String val) {
        if (val == null || val.isEmpty()) {
            return val;
        }
        StringBuilder sb = new StringBuilder(val.length() + 8);
        for (int i = 0; i < val.length(); i++) {
            char c = val.charAt(i);
            if (c == '\\' || c == '%' || c == '_') {
                sb.append('\\');
            }
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * 模糊查询值归一化：只去首尾空白（不删词内空格，"New York" 不能变成 "NewYork"），按需转义通配符
     */
    private String normalizeLikeValue(String val) {
        String trimmed = val.strip();
        return escapeLikeWildcards ? escapeLike(trimmed) : trimmed;
    }

    public LambdaQueryWrapperX<T> likeIfPresent(SFunction<T, ?> column, String val) {
        if (StringUtils.hasText(val)) {
            return (LambdaQueryWrapperX<T>) super.like(column, normalizeLikeValue(val));
        }
        return this;
    }

    public LambdaQueryWrapperX<T> likeRightIfPresent(SFunction<T, ?> column, String val) {
        if (StringUtils.hasText(val)) {
            return (LambdaQueryWrapperX<T>) super.likeRight(column, normalizeLikeValue(val));
        }
        return this;
    }

    public LambdaQueryWrapperX<T> inIfPresent(SFunction<T, ?> column, Collection<?> values) {
        if (CollUtil.isNotEmpty(values)) {
            return (LambdaQueryWrapperX<T>) super.in(column, values);
        }
        return this;
    }

    public LambdaQueryWrapperX<T> inIfPresent(SFunction<T, ?> column, Object... values) {
        if (values == null || values.length == 0) {
            return this;
        }
        // 先剔除 null 元素再判空，避免单个 null 导致整个 IN 条件被丢弃（查询退化为无过滤）
        Object[] nonNullValues = Arrays.stream(values).filter(Objects::nonNull).toArray();
        if (nonNullValues.length > 0) {
            return (LambdaQueryWrapperX<T>) super.in(column, nonNullValues);
        }
        return this;
    }

    public LambdaQueryWrapperX<T> eqIfPresent(SFunction<T, ?> column, Object val) {
        if (ObjectUtil.isNotEmpty(val)) {
            return (LambdaQueryWrapperX<T>) super.eq(column, val);
        }
        return this;
    }

    public LambdaQueryWrapperX<T> neIfPresent(SFunction<T, ?> column, Object val) {
        if (ObjectUtil.isNotEmpty(val)) {
            return (LambdaQueryWrapperX<T>) super.ne(column, val);
        }
        return this;
    }

    public LambdaQueryWrapperX<T> gtIfPresent(SFunction<T, ?> column, Object val) {
        if (val != null) {
            return (LambdaQueryWrapperX<T>) super.gt(column, val);
        }
        return this;
    }

    public LambdaQueryWrapperX<T> geIfPresent(SFunction<T, ?> column, Object val) {
        if (val != null) {
            return (LambdaQueryWrapperX<T>) super.ge(column, val);
        }
        return this;
    }

    public LambdaQueryWrapperX<T> ltIfPresent(SFunction<T, ?> column, Object val) {
        if (val != null) {
            return (LambdaQueryWrapperX<T>) super.lt(column, val);
        }
        return this;
    }

    public LambdaQueryWrapperX<T> leIfPresent(SFunction<T, ?> column, Object val) {
        if (val != null) {
            return (LambdaQueryWrapperX<T>) super.le(column, val);
        }
        return this;
    }

    public LambdaQueryWrapperX<T> betweenIfPresent(SFunction<T, ?> column, Object val1, Object val2) {
        if (val1 != null && val2 != null) {
            return (LambdaQueryWrapperX<T>) super.between(column, val1, val2);
        }
        if (val1 != null) {
            return (LambdaQueryWrapperX<T>) ge(column, val1);
        }
        if (val2 != null) {
            return (LambdaQueryWrapperX<T>) le(column, val2);
        }
        return this;
    }

    public LambdaQueryWrapperX<T> betweenIfPresent(SFunction<T, ?> column, Object[] values) {
        Object val1 = ArrayUtil.get(values, 0);
        Object val2 = ArrayUtil.get(values, 1);
        return betweenIfPresent(column, val1, val2);
    }

    // ========== 重写父类方法，方便链式调用 ==========

    @Override
    public LambdaQueryWrapperX<T> eq(boolean condition, SFunction<T, ?> column, Object val) {
        super.eq(condition, column, val);
        return this;
    }

    @Override
    public LambdaQueryWrapperX<T> eq(SFunction<T, ?> column, Object val) {
        super.eq(column, val);
        return this;
    }

    @Override
    public LambdaQueryWrapperX<T> orderByDesc(SFunction<T, ?> column) {
        super.orderByDesc(true, column);
        return this;
    }

    @Override
    public LambdaQueryWrapperX<T> last(String lastSql) {
        super.last(lastSql);
        return this;
    }

    @Override
    public LambdaQueryWrapperX<T> in(SFunction<T, ?> column, Collection<?> coll) {
        super.in(column, coll);
        return this;
    }

    @Override
    public LambdaQueryWrapperX<T> exists(boolean condition, String existsSql, Object... values) {
        super.exists(condition, existsSql, values);
        return this;
    }

    @Override
    public LambdaQueryWrapperX<T> notExists(boolean condition, String existsSql, Object... values) {
        super.notExists(condition, existsSql, values);
        return this;
    }

    @Override
    public LambdaQueryWrapperX<T> and(boolean condition, Consumer<LambdaQueryWrapper<T>> consumer) {
        super.and(condition, consumer);
        return this;
    }

    @Override
    public LambdaQueryWrapperX<T> or(boolean condition, Consumer<LambdaQueryWrapper<T>> consumer) {
        super.or(condition, consumer);
        return this;
    }

    @Override
    public LambdaQueryWrapperX<T> nested(boolean condition, Consumer<LambdaQueryWrapper<T>> consumer) {
        super.nested(condition, consumer);
        return this;
    }

    @Override
    public LambdaQueryWrapperX<T> not(boolean condition, Consumer<LambdaQueryWrapper<T>> consumer) {
        super.not(condition, consumer);
        return this;
    }

}
