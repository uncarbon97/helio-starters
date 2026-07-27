package cc.uncarbon.framework.helium.db.entity;

import cc.uncarbon.framework.helium.db.constant.EntityField;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Instant;

/**
 * 关联表实体类
 *
 * @author Uncarbon
 */
@Accessors(chain = true)
@Data
public abstract class AbstractRelationEntity implements Entity, AuditingTimeEntity, AuditingUserEntity {

    // 没有逻辑删除

    /**
     * 创建时刻
     */
    @Schema(description = "创建时刻")
    @TableField(value = EntityField.CREATED_AT_COLUMN, fill = FieldFill.INSERT)
    private Instant createdAt;

    /**
     * 创建者
     */
    @Schema(description = "创建者")
    @TableField(value = EntityField.CREATED_BY_COLUMN, fill = FieldFill.INSERT)
    private String createdBy;

    // 没有更新时刻和更新者
    @TableField(exist = false)
    private Instant updatedAt;
    @TableField(exist = false)
    private String updatedBy;
    @Override
    public Instant getUpdatedAt() {
        return null;
    }
    @Override
    public String getUpdatedBy() {
        return null;
    }
}
