package cc.uncarbon.framework.crud.entity;

import cc.uncarbon.framework.core.constant.HelioConstant;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础实体类，默认含[行级租户ID]
 * @param <T> 主键类型，一般用 Long
 *
 * @author Uncarbon
 */
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
public abstract class HelioBaseEntity<T extends Serializable> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


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
    @TableField(value = HelioConstant.CRUD.COLUMN_TENANT_ID, fill = FieldFill.INSERT)
    private Long tenantId;

    /**
     * 乐观锁
     * 需自行加@Version注解才有效
     */
    @Schema(description = "乐观锁", title = "需再次复制本字段，并自行加 @Version 注解才有效")
    @TableField(value = "revision")
    private Long revision;

    /**
     * 逻辑删除标识
     * 0=否 1=是
     */
    @Schema(description = "逻辑删除标识", title = "0=否 1=是")
    @TableLogic
    @TableField(value = "del_flag")
    private Integer delFlag;

    /**
     * 创建时刻
     */
    @Schema(description = "创建时刻")
    @DateTimeFormat(pattern = HelioConstant.Jackson.DATE_TIME_FORMAT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = HelioConstant.Jackson.DATE_TIME_FORMAT)
    @TableField(value = HelioConstant.CRUD.COLUMN_CREATED_AT, fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 创建者
     */
    @Schema(description = "创建者")
    @TableField(value = HelioConstant.CRUD.COLUMN_CREATED_BY, fill = FieldFill.INSERT)
    private String createdBy;

    /**
     * 更新时刻
     */
    @Schema(description = "更新时刻")
    @DateTimeFormat(pattern = HelioConstant.Jackson.DATE_TIME_FORMAT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = HelioConstant.Jackson.DATE_TIME_FORMAT)
    @TableField(value = HelioConstant.CRUD.COLUMN_UPDATED_AT, fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 更新者
     */
    @Schema(description = "更新者")
    @TableField(value = HelioConstant.CRUD.COLUMN_UPDATED_BY, fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;

}
