package cc.uncarbon.framework.crud.entity;

import cc.uncarbon.framework.core.constant.HelioConstant;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

/**
 * 基础实体类，去除[行级租户ID]
 *
 * @author Uncarbon
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
public abstract class HelioNoTenantBaseEntity<PK extends Serializable> extends HelioBaseEntity<PK>  {

    /**
     * 覆盖掉行级租户ID
     */
    @TableField(value = HelioConstant.CRUD.COLUMN_TENANT_ID, exist = false)
    private Long tenantId;

}
