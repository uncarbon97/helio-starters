package cc.uncarbon.framework.helium.db.entity;

import cc.uncarbon.framework.helium.db.constant.EntityField;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Instant;

/**
 * 通用实体类
 *
 * @author Uncarbon
 */
@Data
@Accessors(chain = true)
public abstract class AbstractGenericEntity implements Entity, LogicDelEntity, AuditingTimeEntity, AuditingUserEntity {

    @Schema(description = "逻辑删除标识")
    @TableLogic
    @TableField(value = EntityField.DEL_FLAG_COLUMN)
    private Integer delFlag;

    @Schema(description = "创建时刻")
    @TableField(value = EntityField.CREATED_AT_COLUMN, fill = FieldFill.INSERT)
    private Instant createdAt;

    @Schema(description = "创建者")
    @TableField(value = EntityField.CREATED_BY_COLUMN, fill = FieldFill.INSERT)
    private Long createdBy;

    @Schema(description = "更新时刻")
    @TableField(value = EntityField.UPDATED_AT_COLUMN, fill = FieldFill.UPDATE)
    private Instant updatedAt;

    @Schema(description = "更新者用户ID")
    @TableField(value = EntityField.UPDATED_BY_COLUMN, fill = FieldFill.UPDATE)
    private Long updatedBy;

}
