package cc.uncarbon.framework.helium.db.entity;

import cc.uncarbon.framework.helium.db.constant.EntityField;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 关联表实体类，含有【租户ID】
 *
 * @author Uncarbon
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Data
public abstract class AbstractTenantRelationEntity extends AbstractRelationEntity implements TenantEntity {

    /**
     * 租户ID
     */
    @Schema(description = "租户ID")
    @TableField(value = EntityField.TENANT_ID_COLUMN, fill = FieldFill.INSERT)
    private Long tenantId;

}
