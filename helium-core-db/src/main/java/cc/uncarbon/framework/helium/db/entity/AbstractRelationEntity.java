package cc.uncarbon.framework.helium.db.entity;

import cc.uncarbon.framework.helium.db.constant.EntityField;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 关联表实体类
 *
 * @author Uncarbon
 */
@Accessors(chain = true)
@Data
public abstract class AbstractRelationEntity implements Entity, AuditingTimeEntity, AuditingUserEntity {

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
     * 数据表中无该字段
     */
    @Override
    public LocalDateTime getUpdatedAt() {
        return null;
    }

    /**
     * 数据表中无该字段
     */
    @Override
    public String getUpdatedBy() {
        return null;
    }
}
