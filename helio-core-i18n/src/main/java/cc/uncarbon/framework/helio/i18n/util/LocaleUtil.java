package cc.uncarbon.framework.helio.i18n.util;

import cn.hutool.core.text.CharPool;
import cn.hutool.core.text.CharSequenceUtil;
import lombok.experimental.UtilityClass;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Operations to assist when working with a {@link Locale}.
 *
 * <p>This class tries to handle {@code null} input gracefully.
 * An exception will not be thrown for a {@code null} input.
 * Each method documents its behavior in more detail.</p>
 *
 * @author apache-commons-lang3
 */
@UtilityClass
public class LocaleUtil {


    private static final class SyncAvoid {

        private static final List<Locale> AVAILABLE_LOCALE_ULIST;

        private static final Set<Locale> AVAILABLE_LOCALE_USET;

        static {
            final Locale[] locales = Locale.getAvailableLocales();
            Arrays.sort(locales, Comparator.comparing(Locale::toString));
            AVAILABLE_LOCALE_ULIST = Collections.unmodifiableList(Arrays.asList(locales));
            AVAILABLE_LOCALE_USET = Collections.unmodifiableSet(new LinkedHashSet<>(AVAILABLE_LOCALE_ULIST));
        }
    }

    /**
     * The undetermined language {@value}.
     *
     * @see Locale#toLanguageTag()
     */
    private static final String UNDETERMINED = "und";

    private static final ConcurrentMap<String, List<Locale>> cLanguagesByCountry = new ConcurrentHashMap<>();

    private static final ConcurrentMap<String, List<Locale>> cCountriesByLanguage = new ConcurrentHashMap<>();

    public static List<Locale> availableLocaleList() {
        return SyncAvoid.AVAILABLE_LOCALE_ULIST;
    }

    private static List<Locale> availableLocaleList(final Predicate<Locale> predicate) {
        return availableLocaleList().stream().filter(predicate).collect(Collectors.toList());
    }

    public static Set<Locale> availableLocaleSet() {
        return SyncAvoid.AVAILABLE_LOCALE_USET;
    }

    public static List<Locale> countriesByLanguage(final String languageCode) {
        if (languageCode == null) {
            return Collections.emptyList();
        }
        return cCountriesByLanguage.computeIfAbsent(languageCode, lc -> Collections.unmodifiableList(
                availableLocaleList(locale -> languageCode.equals(locale.getLanguage()) && !locale.getCountry().isEmpty() && locale.getVariant().isEmpty())));
    }

    public static boolean isAvailableLocale(final Locale locale) {
        return availableLocaleSet().contains(locale);
    }

    private static boolean isISO3166CountryCode(final String str) {
        return isAllUpperCase(str) && str.length() == 2;
    }

    private static boolean isISO639LanguageCode(final String str) {
        return isAllLowerCase(str) && (str.length() == 2 || str.length() == 3);
    }

    public static boolean isLanguageUndetermined(final Locale locale) {
        return locale == null || UNDETERMINED.equals(locale.toLanguageTag());
    }

    private static boolean isNumericAreaCode(final String str) {
        return CharSequenceUtil.isNumeric(str) && str.length() == 3;
    }

    public static List<Locale> languagesByCountry(final String countryCode) {
        if (countryCode == null) {
            return Collections.emptyList();
        }
        return cLanguagesByCountry.computeIfAbsent(countryCode,
                k -> Collections.unmodifiableList(availableLocaleList(locale -> countryCode.equals(locale.getCountry()) && locale.getVariant().isEmpty())));
    }

    public static List<Locale> localeLookupList(final Locale locale) {
        return localeLookupList(locale, locale);
    }

    public static List<Locale> localeLookupList(final Locale locale, final Locale defaultLocale) {
        final List<Locale> list = new ArrayList<>(4);
        if (locale != null) {
            list.add(locale);
            if (!locale.getVariant().isEmpty()) {
                list.add(Locale.of(locale.getLanguage(), locale.getCountry()));
            }
            if (!locale.getCountry().isEmpty()) {
                list.add(Locale.of(locale.getLanguage(), CharSequenceUtil.EMPTY));
            }
            if (!list.contains(defaultLocale)) {
                list.add(defaultLocale);
            }
        }
        return Collections.unmodifiableList(list);
    }

    private static Locale parseLocale(final String str) {
        if (isISO639LanguageCode(str)) {
            return Locale.of(str);
        }
        final int limit = 3;
        final char separator = str.indexOf(CharPool.UNDERLINE) != -1 ? CharPool.UNDERLINE : CharPool.DASHED;
        final String[] segments = str.split(String.valueOf(separator), 3);
        final String language = segments[0];
        if (segments.length == 2) {
            final String country = segments[1];
            if (isISO639LanguageCode(language) && isISO3166CountryCode(country) || isNumericAreaCode(country)) {
                return Locale.of(language, country);
            }
        } else if (segments.length == limit) {
            final String country = segments[1];
            final String variant = segments[2];
            if (isISO639LanguageCode(language) &&
                    (country.isEmpty() || isISO3166CountryCode(country) || isNumericAreaCode(country)) &&
                    !variant.isEmpty()) {
                return Locale.of(language, country, variant);
            }
        }
        throw new IllegalArgumentException("Invalid locale format: " + str);
    }

    public static Locale toLocale(final Locale locale) {
        return locale != null ? locale : Locale.getDefault();
    }

    public static Locale toLocale(final String str) {
        if (str == null) {
            return null;
        }
        if (str.isEmpty()) {
            return Locale.of(CharSequenceUtil.EMPTY, CharSequenceUtil.EMPTY);
        }
        if (str.contains("#")) {
            throw new IllegalArgumentException("Invalid locale format: " + str);
        }
        final int len = str.length();
        if (len < 2) {
            throw new IllegalArgumentException("Invalid locale format: " + str);
        }
        final char ch0 = str.charAt(0);
        if (ch0 == CharPool.UNDERLINE || ch0 == CharPool.DASHED) {
            if (len < 3) {
                throw new IllegalArgumentException("Invalid locale format: " + str);
            }
            final char ch1 = str.charAt(1);
            final char ch2 = str.charAt(2);
            if (!Character.isUpperCase(ch1) || !Character.isUpperCase(ch2)) {
                throw new IllegalArgumentException("Invalid locale format: " + str);
            }
            if (len == 3) {
                return Locale.of(CharSequenceUtil.EMPTY, str.substring(1, 3));
            }
            if (len < 5) {
                throw new IllegalArgumentException("Invalid locale format: " + str);
            }
            if (str.charAt(3) != ch0) {
                throw new IllegalArgumentException("Invalid locale format: " + str);
            }
            return Locale.of(CharSequenceUtil.EMPTY, str.substring(1, 3), str.substring(4));
        }

        return parseLocale(str);
    }

    // --- Local implementations for methods not available in Hutool ---

    private static boolean isAllUpperCase(final CharSequence cs) {
        if (CharSequenceUtil.isEmpty(cs)) {
            return false;
        }
        for (int i = 0; i < cs.length(); i++) {
            if (!Character.isUpperCase(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isAllLowerCase(final CharSequence cs) {
        if (CharSequenceUtil.isEmpty(cs)) {
            return false;
        }
        for (int i = 0; i < cs.length(); i++) {
            if (!Character.isLowerCase(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
