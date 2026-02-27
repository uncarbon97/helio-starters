package cc.uncarbon.framework.helio.base.context;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Collection;
import java.util.List;

/**
 * 简单用户上下文
 *
 * @author Uncarbon
 */
@Schema(description = "简单用户上下文")
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SimpleUserContext implements UserContext {


    @Schema(description = "用户ID")
    protected Long userId;

    @Schema(description = "用户账号")
    protected String userName;

    @Schema(description = "用户手机号")
    protected String userPhoneNo;

    @Schema(description = "用户类型编码")
    protected String userTypeCode;

    @Schema(description = "用户角色ID集合", example = "[1, 2, 3]")
    protected Collection<Long> rolesIds;

    @Schema(description = "用户角色编码集合", example = """
            ["Admin", "CEO"]""")
    protected List<String> roleCodes;

    @Schema(description = "用户昵称")
    protected String userNickname;

    @Schema(description = "客户端IP地址")
    protected String clientIP;

}
