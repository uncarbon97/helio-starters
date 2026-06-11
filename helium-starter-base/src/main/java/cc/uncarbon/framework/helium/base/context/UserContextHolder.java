package cc.uncarbon.framework.helium.base.context;

import lombok.experimental.UtilityClass;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * 用户上下文持有者类
 *
 * @author Uncarbon
 */
@UtilityClass
public class UserContextHolder {

    private static final ScopedValue<UserContext> SCOPED = ScopedValue.newInstance();

    @NonNull
    public ScopedValue<UserContext> scoped() {
        return SCOPED;
    }

    /**
     * 获取当前用户上下文
     *
     * @return null or 当前用户上下文
     */
    @Nullable
    public UserContext get() {
        if (SCOPED.isBound()) {
            return SCOPED.get();
        }
        return null;
    }

    /**
     * 获取当前用户上下文的 {@link Optional} 形式
     */
    @NonNull
    public Optional<UserContext> getOptional() {
        return Optional.ofNullable(get());
    }

    /**
     * 快速取当前用户ID
     */
    @Nullable
    public Long getUserId() {
        UserContext context = get();
        return context == null ? null : context.getUserId();
    }

    /**
     * 快速取当前用户名
     */
    @Nullable
    public String getUserPin() {
        UserContext context = get();
        return context == null ? null : context.getUserPin();
    }

}
