package cc.uncarbon.framework.helium.db.mybatisplus.handler;

import cc.uncarbon.framework.helium.base.context.UserContextHolder;
import cc.uncarbon.framework.helium.db.constant.EntityField;
import cc.uncarbon.framework.helium.tenant.context.TenantContextHolder;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;

import java.time.Instant;
import java.util.List;

/**
 * 字段自动填充，摘自 mybatis-plus 官方例程
 *
 * <p>通用填充：租户、创建/更新时间、操作人；派生列等扩展填充见 {@link MetaObjectFiller}</p>
 *
 * @author nieqiurong
 * @author Uncarbon
 */
public class MybatisPlusAutoFillColumnHandler implements MetaObjectHandler {

    /**
     * {@link TenantContextHolder} 的全限定名
     */
    protected static final String TENANT_CONTEXT_HOLDER_FQDN =
            "cc.uncarbon.framework.helium.tenant.context.TenantContextHolder";

    /**
     * 当前 ClassLoader 内，存在 {@link TenantContextHolder}
     */
    protected boolean existsTenantContextHolder = false;

    /**
     * 扩展填充器
     */
    private final List<MetaObjectFiller> extendFillers;


    public MybatisPlusAutoFillColumnHandler(List<MetaObjectFiller> extendFillers) {
        this.extendFillers = List.copyOf(extendFillers);
        try {
            Class.forName(TENANT_CONTEXT_HOLDER_FQDN);
            existsTenantContextHolder = true;
        } catch (ClassNotFoundException ignored) {
        }
    }

    @Override
    public void insertFill(MetaObject metaObject) {
        overrideTenantId(metaObject);
        fillByExtends(metaObject, true);
        strictInsertFill(metaObject, EntityField.CREATED_AT_FIELD, Instant.class, Instant.now());
        Long uid = UserContextHolder.getUserId();
        if (uid != null) {
            strictInsertFill(metaObject, EntityField.CREATED_BY_FIELD, Long.class, uid);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        overrideTenantId(metaObject);
        fillByExtends(metaObject, false);
        // 避免严格填充策略下，实体类字段非空时不更新
        metaObject.setValue(EntityField.UPDATED_AT_FIELD, Instant.now());
        Long uid = UserContextHolder.getUserId();
        if (uid != null) {
            metaObject.setValue(EntityField.UPDATED_BY_FIELD, uid);
        }
    }

    /*
    ----------------------------------------------------------------
                        私有方法
    ----------------------------------------------------------------
     */

    private void fillByExtends(MetaObject metaObject, boolean insert) {
        for (MetaObjectFiller filler : extendFillers) {
            filler.fill(metaObject, insert);
        }
    }

    /**
     * 避免入参携带伪造 tenantId ，造成跨租户写入
     */
    protected void overrideTenantId(MetaObject metaObject) {
        if (existsTenantContextHolder && !TenantContextHolder.isIgnored()) {
            Long currentTenantId = TenantContextHolder.getTenantId();
            if (currentTenantId != null && metaObject.hasSetter(EntityField.TENANT_ID_FIELD)) {
                setFieldValByName(EntityField.TENANT_ID_FIELD, currentTenantId, metaObject);
            }
        }
    }

}
