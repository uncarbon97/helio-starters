package cc.uncarbon.framework.crud.service.impl;

import cc.uncarbon.framework.crud.entity.HelioBaseEntity;
import cc.uncarbon.framework.crud.service.HelioBaseService;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.NoArgsConstructor;

/**
 * 服务实现类基础模板
 * M = MAPPER 持久层 interface
 * E = ENTITY 实体类
 *
 * @author Uncarbon
 */
@NoArgsConstructor
public class HelioBaseServiceImpl<M extends BaseMapper<E>, E extends HelioBaseEntity<?>> extends ServiceImpl<M, E> implements HelioBaseService<E> {

}
