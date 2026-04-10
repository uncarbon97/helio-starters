package cc.uncarbon.framework.helio.db.mybatisplus.handler;

import cc.uncarbon.framework.helio.base.context.UserContextHolder;
import cc.uncarbon.framework.helio.db.constant.EntityField;
import cc.uncarbon.framework.helio.tenant.context.TenantContextHolder;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;

/**
 * 字段自动填充，摘自Mybatis-Plus官方例程
 *
 * @author nieqiurong
 * @author Uncarbon
 */
public class MybatisPlusAutoFillColumnHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, EntityField.TENANT_ID_FIELD, Long.class, TenantContextHolder.getTenantId());
        this.strictInsertFill(metaObject, EntityField.CREATED_AT_FIELD, LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, EntityField.CREATED_BY_FIELD, String.class, UserContextHolder.getUserName());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, EntityField.UPDATED_AT_FIELD, LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, EntityField.UPDATED_BY_FIELD, String.class, UserContextHolder.getUserName());
    }

}
