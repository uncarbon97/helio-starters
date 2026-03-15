package cc.uncarbon.framework.helio.mybatis.entity;

import cc.uncarbon.framework.helio.mybatis.constant.EntityField;
import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 基础实体类，默认含[行级租户ID]
 * @param <T> 主键类型，一般用 Long
 *
 * @author Uncarbon
 */
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public abstract class BaseEntity<T>  {


    /**
     * 主键ID
     */
    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private T id;

    /**
     * 行级租户ID
     */
    @Schema(description = "行级租户ID")
    @TableField(value = EntityField.COLUMN_TENANT_ID, fill = FieldFill.INSERT)
    private Long tenantId;

    /**
     * 逻辑删除标识
     * 0=否 1=是
     */
    @Schema(description = "逻辑删除标识(0=否 1=是)")
    @TableLogic
    @TableField(value = "del_flag")
    private Integer delFlag;

    /**
     * 创建时刻
     */
    @Schema(description = "创建时刻")
    @TableField(value = EntityField.COLUMN_CREATED_BY, fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 创建者
     */
    @Schema(description = "创建者")
    @TableField(value = EntityField.COLUMN_CREATED_BY, fill = FieldFill.INSERT)
    private String createdBy;

    /**
     * 更新时刻
     */
    @Schema(description = "更新时刻")
    @TableField(value = EntityField.COLUMN_UPDATED_AT, fill = FieldFill.UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 更新者
     */
    @Schema(description = "更新者")
    @TableField(value = EntityField.COLUMN_UPDATED_BY, fill = FieldFill.UPDATE)
    private String updatedBy;

}
