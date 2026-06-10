package cc.uncarbon.framework.helium.web.context;

import lombok.experimental.UtilityClass;

import java.util.Optional;

/**
 * 访客上下文持有者类
 *
 * @author Uncarbon
 */
@UtilityClass
public class VisitorContextHolder {

    private static final ScopedValue<VisitorContext> SCOPED = ScopedValue.newInstance();

    public ScopedValue<VisitorContext> scope() {
        return SCOPED;
    }

    /**
     * 获取当前访客上下文
     *
     * @return null or 当前访客上下文
     */
    public VisitorContext get() {
        if (SCOPED.isBound()) {
            return SCOPED.get();
        }
        return null;
    }

    /**
     * 获取当前访客上下文的 {@link Optional} 形式
     */
    public Optional<VisitorContext> getOptional() {
        return Optional.ofNullable(get());
    }
}
