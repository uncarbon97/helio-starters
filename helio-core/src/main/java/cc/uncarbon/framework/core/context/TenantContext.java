package cc.uncarbon.framework.core.context;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;

/**
 * 当前租户上下文对象
 *
 * @author Uncarbon
 */
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TenantContext implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String CAMEL_NAME = "tenantContext";

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "租户名")
    private String tenantName;

}
