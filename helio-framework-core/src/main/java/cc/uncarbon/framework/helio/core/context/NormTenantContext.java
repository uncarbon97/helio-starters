package cc.uncarbon.framework.helio.core.context;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 常规租户上下文对象
 *
 * @author Uncarbon
 */
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class NormTenantContext implements TenantContext {

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 租户名
     */
    private String tenantName;

    /**
     * 租户编码
     */
    private String tenantCode;

}
