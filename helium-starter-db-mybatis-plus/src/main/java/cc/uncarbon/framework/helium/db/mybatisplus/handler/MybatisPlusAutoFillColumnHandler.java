package cc.uncarbon.framework.helium.db.mybatisplus.handler;

import cc.uncarbon.framework.helium.base.context.UserContextHolder;
import cc.uncarbon.framework.helium.db.constant.EntityField;
import cc.uncarbon.framework.helium.tenant.context.TenantContextHolder;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;

import java.time.Instant;

/**
 * 字段自动填充，摘自Mybatis-Plus官方例程
 *
 * <p>审计时刻使用 {@link Instant}（绝对时刻，UTC），DB 列约定为 {@code TIMESTAMP}，
 * 由 MyBatis 内建 {@code InstantTypeHandler} 完成与 TIMESTAMP 的换算。
 *
 * @author nieqiurong
 * @author Uncarbon
 */
public class MybatisPlusAutoFillColumnHandler implements MetaObjectHandler {

    /**
     * {@link TenantContextHolder} 的全限定名
     */
    private static final String TENANT_CONTEXT_HOLDER_FQDN =
            "cc.uncarbon.framework.helium.tenant.context.TenantContextHolder";

    /**
     * 当前 ClassLoader 内，存在 {@link TenantContextHolder}
     */
    private boolean existsTenantContextHolder = false;

    public MybatisPlusAutoFillColumnHandler() {
        try {
            Class.forName(TENANT_CONTEXT_HOLDER_FQDN);
            this.existsTenantContextHolder = true;
        } catch (ClassNotFoundException ignored) {
        }
    }

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, EntityField.CREATED_AT_FIELD, Instant.class, Instant.now());
        this.strictInsertFill(metaObject, EntityField.CREATED_BY_FIELD, String.class, UserContextHolder.getUserPin());

        if (this.existsTenantContextHolder) {
            this.strictInsertFill(metaObject, EntityField.TENANT_ID_FIELD, Long.class, TenantContextHolder.getTenantId());
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, EntityField.UPDATED_AT_FIELD, Instant.class, Instant.now());
        this.strictUpdateFill(metaObject, EntityField.UPDATED_BY_FIELD, String.class, UserContextHolder.getUserPin());
    }
}
