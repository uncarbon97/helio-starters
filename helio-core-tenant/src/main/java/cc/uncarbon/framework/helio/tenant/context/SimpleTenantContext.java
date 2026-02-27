package cc.uncarbon.framework.helio.tenant.context;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 简单租户上下文
 *
 * @author Uncarbon
 */
@Schema(description = "简单租户上下文")
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SimpleTenantContext implements TenantContext {

    /**
     * 租户ID
     */
    protected Long tenantId;

    /**
     * 租户名
     */
    protected String tenantName;

    /**
     * 租户编码
     */
    protected String tenantCode;

}
