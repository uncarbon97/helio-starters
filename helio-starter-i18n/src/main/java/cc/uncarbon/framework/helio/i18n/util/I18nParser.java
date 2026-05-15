package cc.uncarbon.framework.helio.i18n.util;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.text.StrPool;
import lombok.experimental.UtilityClass;

import java.time.ZoneId;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;

/**
 * 国际化用到的解析器工具类
 *
 * @author Uncarbon
 */
@UtilityClass
public class I18nParser {

    public Optional<Locale> parseLocale(String value) {
        if (CharSequenceUtil.isBlank(value)) {
            return Optional.empty();
        }
        try {
            String trimmed = value.trim();
            // 支持 en_US 或 en-US 格式
            if (trimmed.contains(StrPool.DASHED)) {
                String[] parts = trimmed.split(StrPool.DASHED, 2);
                return Optional.of(Locale.of(parts[0], parts[1]));
            }
            if (trimmed.contains(StrPool.UNDERLINE)) {
                String[] parts = trimmed.split(StrPool.UNDERLINE, 2);
                return Optional.of(Locale.of(parts[0], parts[1]));
            }
            return Optional.of(Locale.of(trimmed));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<TimeZone> parseTimeZone(String value) {
        if (CharSequenceUtil.isBlank(value)) {
            return Optional.empty();
        }
        try {
            return Optional.of(TimeZone.getTimeZone(ZoneId.of(value.trim())));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
