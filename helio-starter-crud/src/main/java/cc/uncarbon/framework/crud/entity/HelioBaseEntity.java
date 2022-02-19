package cc.uncarbon.framework.crud.entity;

import cc.uncarbon.framework.core.constant.HelioConstant;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 含[行级租户ID]的基础实体类
 *
 * @author Uncarbon
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
public abstract class HelioBaseEntity<PK extends Serializable> extends HelioNoTenantBaseEntity<PK> {

    /**
     * 行级租户ID
     */
    @ApiModelProperty(value = "行级租户ID")
    @TableField(value = HelioConstant.CRUD.COLUMN_TENANT_ID, fill = FieldFill.INSERT)
    private Long tenantId;

}
