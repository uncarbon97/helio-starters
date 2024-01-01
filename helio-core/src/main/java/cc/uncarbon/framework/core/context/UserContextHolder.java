package cc.uncarbon.framework.core.context;

import com.alibaba.ttl.TransmittableThreadLocal;
import lombok.experimental.UtilityClass;

import java.util.Optional;

/**
 * 用户上下文持有者类
 *
 * @author Uncarbon
 */
@UtilityClass
public class UserContextHolder {

    private final TransmittableThreadLocal<UserContext> THREAD_LOCAL_CONTEXT = new TransmittableThreadLocal<>();


    /**
     * 获取当前用户上下文
     *
     * @return null or 当前用户上下文
     */
    public UserContext getUserContext() {
        return THREAD_LOCAL_CONTEXT.get();
    }

    /**
     * 获取当前用户上下文
     */
    public Optional<UserContext> getUserContextOptional() {
        return Optional.ofNullable(THREAD_LOCAL_CONTEXT.get());
    }

    /**
     * 设置当前用户上下文
     *
     * @param newContext 新上下文，传 null 则为清除
     */
    public void setUserContext(UserContext newContext) {
        if (newContext == null) {
            THREAD_LOCAL_CONTEXT.remove();
            return;
        }

        THREAD_LOCAL_CONTEXT.set(newContext);
    }

    /**
     * 强制清空本线程的用户上下文，防止影响被线程池复用的其他线程，以及内存泄露
     */
    public void clear() {
        setUserContext(null);
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
