package cc.uncarbon.framework.helium.i18n.jackson;

import cc.uncarbon.framework.helium.i18n.context.I18nContext;
import cc.uncarbon.framework.helium.i18n.context.I18nContextHolder;
import cc.uncarbon.framework.helium.i18n.context.timezone.TimezoneInfo;
import cc.uncarbon.framework.helium.jackson.module.InstantNoMillisFormatModule;
import org.jspecify.annotations.NonNull;

import java.time.ZoneId;

/**
 * 时区从 {@link I18nContextHolder} 获取
 *
 * @author Uncarbon
 */
public class InstantTimezoneAwareModule extends InstantNoMillisFormatModule {
    private InstantTimezoneAwareModule() {
        super();
    }

    @Override
    public @NonNull ZoneId resolveDisplayZone() {
        return I18nContextHolder.getContextOptional()
                .map(I18nContext::getTimezoneInfo)
                .map(TimezoneInfo::getZoneId)
                .orElse(OS_ZONE);
    }
}
