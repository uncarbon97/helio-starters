package cc.uncarbon.framework.core.context;

import cn.hutool.core.lang.Dict;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * 当前用户上下文对象
 *
 * @author Uncarbon
 */
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserContext implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String CAMEL_NAME = "userContext";

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户账号")
    private String userName;

    @Schema(description = "用户手机号")
    private String userPhoneNo;

    @Schema(description = "用户类型")
    private String userTypeStr;

    @Schema(description = "用户拥有角色ID", title = "后台管理使用", example = "[1, 2, 3]")
    private Set<Long> rolesIds;

    @Schema(description = "用户拥有角色名称", title = "后台管理使用", example = "[SuperAdmin, Admin, CEO]")
    private List<String> roles;

    @Schema(description = "附加数据")
    private Dict extraData;

    @Schema(description = "客户端IP地址")
    private String clientIP;

}
