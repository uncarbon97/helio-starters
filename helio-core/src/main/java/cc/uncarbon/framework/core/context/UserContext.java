package cc.uncarbon.framework.core.context;

import cc.uncarbon.framework.core.constant.HelioConstant;
import cc.uncarbon.framework.core.enums.HelioBaseEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 当前用户上下文对象
 * @author Uncarbon
 */
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserContext implements Serializable {

    private static final long SerialVersionUID = 1L;

    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "用户账号")
    private String userName;

    @ApiModelProperty(value = "用户手机号")
    private String userPhoneNo;

    @ApiModelProperty(value = "用户类型")
    private HelioBaseEnum<?> userType;

    @ApiModelProperty(value = "用户拥有角色(后台管理使用)")
    private List<String> roles;

    @ApiModelProperty(value = "用户拥有权限(后台管理使用, 决定可见菜单及按钮)")
    private List<String> permissions;

    @ApiModelProperty(value = "附加数据")
    private Map<String, String> extraData;

    @ApiModelProperty(value = "所属租户")
    private TenantContext relationalTenant = TenantContext.builder()
            .tenantId(HelioConstant.CRUD.DEFAULT_TENANT_ID)
            .tenantName(HelioConstant.CRUD.DEFAULT_TENANT_NAME)
            .build();

    @ApiModelProperty(value = "客户端IP地址")
    private String clientIP;

}
