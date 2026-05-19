package cc.uncarbon.framework.helio.web.context;

import com.alibaba.ttl.TransmittableThreadLocal;
import lombok.experimental.UtilityClass;

import java.util.Optional;

/**
 * 访客上下文持有者类
 *
 * @author Uncarbon
 */
@UtilityClass
public class VisitorContextHolder {

    private static final TransmittableThreadLocal<VisitorContext> THREAD_LOCAL_CONTEXT = new TransmittableThreadLocal<>();


    /**
     * 强制清空本线程的访客上下文，防止影响被线程池复用的其他线程，以及内存泄露
     */
    public void clear() {
        setVisitorContext(null);
    }

    /**
     * 获取当前访客上下文
     *
     * @return null or 当前访客上下文
     */
    public VisitorContext getVisitorContext() {
        return THREAD_LOCAL_CONTEXT.get();
    }

    /**
     * 获取当前访客上下文
     */
    public Optional<VisitorContext> getVisitorContextOptional() {
        return Optional.ofNullable(THREAD_LOCAL_CONTEXT.get());
    }

    /**
     * 设置当前访客上下文
     *
     * @param newContext 新上下文，传 null 则为清除
     */
    public void setVisitorContext(VisitorContext newContext) {
        if (newContext == null) {
            THREAD_LOCAL_CONTEXT.remove();
            return;
        }

        THREAD_LOCAL_CONTEXT.set(newContext);
    }
}
