package dev.signalshards.livingworld.core.i18n;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageCatalogTest {
    @Test
    void loadsBrazilianPortugueseMessages() {
        MessageCatalog catalog = new MessageCatalog(Locale.forLanguageTag("pt-BR"));

        assertEquals("Living World ativado.", catalog.text("plugin.enabled"));
    }

    @Test
    void loadsEnglishAsAdditionalLocale() {
        MessageCatalog catalog = new MessageCatalog(Locale.forLanguageTag("en-US"));

        assertEquals("Living World enabled.", catalog.text("plugin.enabled"));
    }

    @Test
    void fallsBackToBrazilianPortugueseForUnsupportedLocales() {
        MessageCatalog catalog = new MessageCatalog(Locale.forLanguageTag("fr-FR"));

        assertEquals("Living World ativado.", catalog.text("plugin.enabled"));
    }

    @Test
    void returnsKeyWhenMessageDoesNotExist() {
        MessageCatalog catalog = new MessageCatalog(Locale.forLanguageTag("en-US"));

        assertEquals("missing.message", catalog.text("missing.message"));
    }
}
