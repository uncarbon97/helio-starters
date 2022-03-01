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
     * @throws NullPointerException 链式调用时需要注意可能存在 NPE 问题
     * @return null or 当前用户上下文
     */
    public UserContext getUserContext() throws NullPointerException {
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
     * 捷径API-取当前用户ID
     *
     * @return null or 当前用户ID
     */
    public Long getUserId() {
        UserContext context = getUserContext();
        return context == null ? null : context.getUserId();
    }

    /**
     * 捷径API-取当前用户名
     *
     * @return null or 当前用户名
     */
    public String getUserName() {
        UserContext context = getUserContext();
        return context == null ? null : context.getUserName();
    }

    /**
     * 捷径API-取当前用户手机号
     *
     * @return null or 当前用户手机号
     */
    public String getUserPhoneNo() {
        UserContext context = getUserContext();
        return context == null ? null : context.getUserPhoneNo();
    }

    /**
     * 捷径API-取当前用户 IP 地址
     *
     * @return null or 当前用户 IP 地址
     */
    public String getClientIP() {
        UserContext context = getUserContext();
        return context == null ? null : context.getClientIP();
    }

}
