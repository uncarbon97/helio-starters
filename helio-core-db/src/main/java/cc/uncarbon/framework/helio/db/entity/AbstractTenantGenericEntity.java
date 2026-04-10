package cc.uncarbon.framework.helio.db.entity;

import cc.uncarbon.framework.helio.db.constant.EntityField;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

/**
 * 通用实体类，含有【行级租户ID】
 *
 * @author Uncarbon
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
public abstract class AbstractTenantGenericEntity extends AbstractGenericEntity implements TenantEntity {

    /**
     * 行级租户 ID
     */
    @Schema(description = "行级租户ID")
    @TableField(value = EntityField.TENANT_ID_COLUMN, fill = FieldFill.INSERT)
    private Long tenantId;

}
