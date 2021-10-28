package cc.uncarbon.framework.crud.service.impl;

import cc.uncarbon.framework.crud.entity.HelioBaseEntity;
import cc.uncarbon.framework.crud.service.HelioBaseService;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.NoArgsConstructor;

/**
 * @author Uncarbon
 */
@NoArgsConstructor
public class HelioBaseServiceImpl<MAPPER extends BaseMapper<ENTITY>, ENTITY extends HelioBaseEntity<?>> extends ServiceImpl<MAPPER, ENTITY> implements HelioBaseService<ENTITY> {

}
