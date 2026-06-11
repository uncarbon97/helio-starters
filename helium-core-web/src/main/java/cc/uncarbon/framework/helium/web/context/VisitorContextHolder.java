package cc.uncarbon.framework.helium.web.context;

import lombok.experimental.UtilityClass;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * 访客上下文持有者类
 *
 * @author Uncarbon
 */
@UtilityClass
public class VisitorContextHolder {

    private static final ScopedValue<VisitorContext> SCOPED = ScopedValue.newInstance();

    @NonNull
    public ScopedValue<VisitorContext> scoped() {
        return SCOPED;
    }

    /**
     * 获取当前访客上下文
     */
    @Nullable
    public VisitorContext get() {
        if (SCOPED.isBound()) {
            return SCOPED.get();
        }
        return null;
    }

    /**
     * 获取当前访客上下文的 {@link Optional} 形式
     */
    @NonNull
    public Optional<VisitorContext> getOptional() {
        return Optional.ofNullable(get());
    }
}
