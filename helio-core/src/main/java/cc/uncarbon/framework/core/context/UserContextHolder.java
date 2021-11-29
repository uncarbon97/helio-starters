package cc.uncarbon.framework.core.context;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.ttl.TransmittableThreadLocal;
import lombok.experimental.UtilityClass;

import java.util.Optional;

/**
 * 存储当前用户上下文
 * @author Uncarbon
 */
@UtilityClass
public class UserContextHolder {

    private final TransmittableThreadLocal<UserContext> THREAD_LOCAL_USER = new TransmittableThreadLocal<>();


    /**
     * 设置当前用户上下文
     * @param userContext 当前用户上下文
     */
    public void setUserContext(UserContext userContext) {
        if (userContext == null) {
            THREAD_LOCAL_USER.remove();
            return;
        }
        THREAD_LOCAL_USER.set(userContext);
    }

    /**
     * 获取当前用户上下文, 确保不返回空
     * @return 当前用户上下文
     */
    public UserContext getUserContext() {
        /*
        必须新创建，否则可能出现租户ID无法切换的BUG
        不能用 .orElse ，即使条件不满足也会new
         */
        return Optional.ofNullable(THREAD_LOCAL_USER.get()).orElseGet(UserContext::new);
    }

    /**
     * 捷径-获取用户ID
     */
    public Long getUserId() {
        return getUserContext().getUserId();
    }

    /**
     * 捷径-获取用户名
     */
    public String getUserName() {
        return getUserContext().getUserName();
    }

    /**
     * 捷径-获取用户手机号
     */
    public String getUserPhoneNo() {
        return getUserContext().getUserPhoneNo();
    }

    /**
     * 捷径-获取所属租户
     */
    public TenantContext getRelationalTenant() {
        return getUserContext().getRelationalTenant();
    }

    /**
     * 捷径-设置所属租户
     */
    public void setRelationalTenant(TenantContext newTenantContext) {
        getUserContext().setRelationalTenant(newTenantContext);
    }

}
