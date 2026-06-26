package cc.uncarbon.framework.helium.db.entity;

import cc.uncarbon.framework.helium.db.constant.EntityField;
import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 通用实体类
 *
 * @author Uncarbon
 */
@Accessors(chain = true)
@Data
public abstract class AbstractGenericEntity implements Entity, AuditingTimeEntity, AuditingUserEntity {

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
