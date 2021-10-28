package cc.uncarbon.framework.crud.handler;

import cc.uncarbon.framework.core.constant.HelioConstant;
import cc.uncarbon.framework.core.context.UserContextHolder;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;

/**
 * 字段自动填充, 摘自Mybatis-Plus官方例程
 * @author nieqiurong
 * @author Uncarbon
 */
public class MybatisPlusAutoFillColumnHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, HelioConstant.CRUD.ENTITY_FIELD_TENANT_ID, Long.class, UserContextHolder.getRelationalTenant().getTenantId());
        this.strictInsertFill(metaObject, HelioConstant.CRUD.ENTITY_FIELD_CREATED_AT, LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, HelioConstant.CRUD.ENTITY_FIELD_CREATED_BY, String.class, UserContextHolder.getUserName());

        // 同时加入更新字段
        this.updateFill(metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, HelioConstant.CRUD.ENTITY_FIELD_UPDATED_AT, LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, HelioConstant.CRUD.ENTITY_FIELD_UPDATED_BY, String.class, UserContextHolder.getUserName());
    }

}
