package cc.uncarbon.framework.helio.db.entity;

import cc.uncarbon.framework.helio.db.constant.EntityField;
import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 通用实体类
 *
 * @author Uncarbon
 */
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public abstract class AbstractGenericEntity implements Entity, LogicDelEntity, AuditingTimeEntity, AuditingUserEntity {

    /**
     * 逻辑删除标识
     * 0=否 1=是
     */
    @Schema(description = "逻辑删除标识(0=否 1=是)")
    @TableLogic
    @TableField(value = EntityField.DEL_FLAG_COLUMN)
    private Integer delFlag;

    /**
     * 创建时刻
     */
    @Schema(description = "创建时刻")
    @TableField(value = EntityField.CREATED_AT_COLUMN, fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 创建者
     */
    @Schema(description = "创建者")
    @TableField(value = EntityField.CREATED_BY_COLUMN, fill = FieldFill.INSERT)
    private String createdBy;

    /**
     * 更新时刻
     */
    @Schema(description = "更新时刻")
    @TableField(value = EntityField.UPDATED_AT_COLUMN, fill = FieldFill.UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 更新者
     */
    @Schema(description = "更新者")
    @TableField(value = EntityField.UPDATED_BY_COLUMN, fill = FieldFill.UPDATE)
    private String updatedBy;

}
