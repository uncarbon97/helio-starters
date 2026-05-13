package cc.uncarbon.framework.helio.db.entity;

import cc.uncarbon.framework.helio.db.constant.EntityField;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 关联表实体类，含有【租户ID】
 *
 * @author Uncarbon
 */
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class AbstractTenantRelationEntity extends AbstractRelationEntity implements TenantEntity {

    /**
     * 租户ID
     */
    @Schema(description = "租户ID")
    @TableField(value = EntityField.TENANT_ID_COLUMN, fill = FieldFill.INSERT)
    private Long tenantId;

}
