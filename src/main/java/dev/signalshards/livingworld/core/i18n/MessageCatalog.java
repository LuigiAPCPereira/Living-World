package dev.signalshards.livingworld.core.i18n;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public final class MessageCatalog {
    private static final String BUNDLE_NAME = "messages";
    private static final Locale DEFAULT_LOCALE = Locale.forLanguageTag("pt-BR");
    private static final ResourceBundle.Control NO_DEFAULT_LOCALE_FALLBACK =
            ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_DEFAULT);

    private final Locale locale;
    private final ResourceBundle messages;
    private final ResourceBundle fallback;

    public MessageCatalog(Locale locale) {
        this.locale = locale;
        this.messages = ResourceBundle.getBundle(BUNDLE_NAME, locale, NO_DEFAULT_LOCALE_FALLBACK);
        this.fallback = ResourceBundle.getBundle(BUNDLE_NAME, DEFAULT_LOCALE, NO_DEFAULT_LOCALE_FALLBACK);
    }

    public static MessageCatalog fromLanguageTag(String languageTag) {
        Locale requested = Locale.forLanguageTag(languageTag);
        return new MessageCatalog(requested.getLanguage().isBlank() ? DEFAULT_LOCALE : requested);
    }

    public String text(String key, Object... arguments) {
        String pattern = lookup(key);
        MessageFormat format = new MessageFormat(pattern, locale);
        return format.format(arguments);
    }

    public Component component(TextColor color, String key, Object... arguments) {
        return Component.text(text(key, arguments), color);
    }

    private String lookup(String key) {
        try {
            return messages.getString(key);
        } catch (MissingResourceException ignored) {
            try {
                return fallback.getString(key);
            } catch (MissingResourceException missingFallback) {
                return key;
            }
        }
    }
}
