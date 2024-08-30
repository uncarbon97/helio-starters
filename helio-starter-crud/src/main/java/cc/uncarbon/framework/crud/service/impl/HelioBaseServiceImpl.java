package cc.uncarbon.framework.crud.service.impl;

import cc.uncarbon.framework.crud.entity.HelioBaseEntity;
import cc.uncarbon.framework.crud.service.HelioBaseService;
import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * 服务实现类基础模板
 * @param <M> 持久层接口
 * @param <E> 实体类
 *
 * @author Uncarbon
 */
@NoArgsConstructor
public class HelioBaseServiceImpl<M extends BaseMapper<E>, E extends HelioBaseEntity<?>>
        extends ServiceImpl<M, E>
        implements HelioBaseService<E> {

    /**
     * 解决MP自带的本方法，自动填充字段不生效问题
     * <a href="https://cloud.tencent.com/developer/article/1930108">参考文章</a>
     */
    @Override
    public boolean update(Wrapper<E> updateWrapper) {
        E entity = updateWrapper.getEntity();
        if (Objects.isNull(entity)) {
            entity = ReflectUtil.newInstance(this.getEntityClass());
        }
        return update(entity, updateWrapper);
    }

}
