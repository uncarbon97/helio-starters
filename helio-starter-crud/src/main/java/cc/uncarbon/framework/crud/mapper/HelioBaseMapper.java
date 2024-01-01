package cc.uncarbon.framework.crud.mapper;

import cc.uncarbon.framework.core.function.StreamFunction;
import cc.uncarbon.framework.crud.entity.HelioBaseEntity;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 持久层基础模板，在Mybatis-Plus BaseMapper上进一步增加功能
 * E = ENTITY 实体类
 * T = 实体类主键ID类型
 *
 * @author Uncarbon
 */
public interface HelioBaseMapper<E extends HelioBaseEntity<T>, T extends Serializable> extends BaseMapper<E> {

    default List<E> selectByIds(Collection<T> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return this.selectList(new LambdaQueryWrapper<E>().in(HelioBaseEntity::getId, ids));
    }

    /**
     * 取ID👉名 映射map
     */
    default Map<T, String> getNameMap(Class<E> entityClass, Collection<T> ids,
                                      Function<E, T> idMapper, Function<E, String> nameMapper) {

        List<E> entityList = this.selectByIds(ids);
        if (CollUtil.isEmpty(entityList)) {
            return Collections.emptyMap();
        }
        return entityList.stream().collect(Collectors.toMap(idMapper, nameMapper, StreamFunction.ignoredThrowingMerger()));
    }
}
