package cc.uncarbon.framework.helium.db.mybatisplus.handler;

import cc.uncarbon.framework.helium.base.context.UserContextHolder;
import cc.uncarbon.framework.helium.db.constant.EntityField;
import cc.uncarbon.framework.helium.tenant.context.TenantContextHolder;
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

    /**
     * TenantContextHolder 的全限定名
     */
    private static final String TENANT_CONTEXT_HOLDER_FULLY_QUALIFIED_NAME =
            "cc.uncarbon.framework.helium.tenant.context.TenantContextHolder";

    /**
     * 当前 ClassLoader 内，存在 TenantContextHolder
     */
    private boolean existsTenantContextHolder = false;

    public MybatisPlusAutoFillColumnHandler() {
        try {
            Class.forName(TENANT_CONTEXT_HOLDER_FULLY_QUALIFIED_NAME);
            this.existsTenantContextHolder = true;
        } catch (ClassNotFoundException ignored) {
        }
    }

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, EntityField.CREATED_AT_FIELD, LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, EntityField.CREATED_BY_FIELD, String.class, UserContextHolder.getUserPin());

        if (this.existsTenantContextHolder) {
            this.strictInsertFill(metaObject, EntityField.TENANT_ID_FIELD, Long.class, TenantContextHolder.getTenantId());
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, EntityField.UPDATED_AT_FIELD, LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, EntityField.UPDATED_BY_FIELD, String.class, UserContextHolder.getUserPin());
    }

}
