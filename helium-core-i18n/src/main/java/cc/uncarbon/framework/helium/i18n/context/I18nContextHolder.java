package cc.uncarbon.framework.helium.i18n.context;

import lombok.experimental.UtilityClass;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * 国际化上下文持有者类
 *
 * @author Uncarbon
 */
@UtilityClass
public class I18nContextHolder {

    private static final ScopedValue<I18nContext> SCOPED = ScopedValue.newInstance();

    @NonNull
    public ScopedValue<I18nContext> scoped() {
        return SCOPED;
    }

    /**
     * 获取当前国际化上下文
     */
    @Nullable
    public I18nContext getContext() {
        if (SCOPED.isBound()) {
            return SCOPED.get();
        }
        return null;
    }

    /**
     * 获取当前国际化上下文的 {@link Optional} 形式
     */
    @NonNull
    public Optional<I18nContext> getContextOptional() {
        return Optional.ofNullable(getContext());
    }
}
