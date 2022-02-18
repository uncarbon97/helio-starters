package cc.uncarbon.framework.core.context;

import com.alibaba.ttl.TransmittableThreadLocal;
import lombok.experimental.UtilityClass;

/**
 * 用户上下文持有者类
 *
 * @author Uncarbon
 */
@UtilityClass
public class UserContextHolder {

    private final TransmittableThreadLocal<UserContext> THREAD_LOCAL_USER = new TransmittableThreadLocal<>();


    /**
     * 获取当前用户上下文
     *
     * @return null or 当前用户上下文
     */
    public UserContext getUserContext() {
        return THREAD_LOCAL_USER.get();
    }

    /**
     * 设置当前用户上下文
     *
     * @param userContext 新上下文，传 null 则为清除
     */
    public void setUserContext(UserContext userContext) {
        if (userContext == null) {
            THREAD_LOCAL_USER.remove();
            return;
        }

        THREAD_LOCAL_USER.set(userContext);
    }

    /**
     * 捷径-获取用户ID
     *
     * @throws NullPointerException 未登录或用户上下文时，调用本 API，易出现
     */
    public Long getUserId() throws NullPointerException {
        return getUserContext().getUserId();
    }

    /**
     * 捷径-获取用户名
     *
     * @throws NullPointerException 未登录或用户上下文时，调用本 API，易出现
     */
    public String getUserName() throws NullPointerException {
        return getUserContext().getUserName();
    }

    /**
     * 捷径-获取用户手机号
     *
     * @throws NullPointerException 未登录或用户上下文时，调用本 API，易出现
     */
    public String getUserPhoneNo() throws NullPointerException {
        return getUserContext().getUserPhoneNo();
    }

}
