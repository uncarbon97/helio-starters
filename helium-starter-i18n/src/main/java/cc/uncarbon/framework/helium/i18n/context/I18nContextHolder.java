package cc.uncarbon.framework.helium.i18n.context;

import com.alibaba.ttl.TransmittableThreadLocal;
import lombok.experimental.UtilityClass;

import java.util.Optional;

/**
 * 国际化上下文持有者类
 *
 * @author Uncarbon
 */
@UtilityClass
public class I18nContextHolder {

    private static final TransmittableThreadLocal<I18nContext> THREAD_LOCAL_CONTEXT = new TransmittableThreadLocal<>();


    /**
     * 强制清空本线程的国际化上下文，防止影响被线程池复用的其他线程，以及内存泄露
     */
    public void clear() {
        setI18nContext(null);
    }

    /**
     * 获取当前国际化上下文
     *
     * @return null or 当前国际化上下文
     */
    public I18nContext getI18nContext() {
        return THREAD_LOCAL_CONTEXT.get();
    }

    /**
     * 获取当前国际化上下文
     */
    public Optional<I18nContext> getI18nContextOptional() {
        return Optional.ofNullable(THREAD_LOCAL_CONTEXT.get());
    }

    /**
     * 设置当前国际化上下文
     *
     * @param newContext 新上下文，传 null 则为清除
     */
    public void setI18nContext(I18nContext newContext) {
        if (newContext == null) {
            THREAD_LOCAL_CONTEXT.remove();
            return;
        }

        THREAD_LOCAL_CONTEXT.set(newContext);
    }
}
