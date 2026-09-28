package dev.signalshards.livingworld.core.companion.paper;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaperResourcePackDeliverySettingsLoaderTest {
    @Test
    void defaultEhTotalmenteDesligado() {
        PaperResourcePackDeliverySettings settings =
                PaperResourcePackDeliverySettingsLoader.load(
                        new YamlConfiguration()
                );

        assertFalse(settings.enabled());
        assertFalse(settings.required());
        assertEquals("", settings.url());
        assertEquals("", settings.sha1());
    }

    @Test
    void carregaDeliveryOptInValido() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("companions.resource-pack.enabled", true);
        config.set(
                "companions.resource-pack.url",
                "https://example.invalid/living-world.zip"
        );
        config.set("companions.resource-pack.sha1", "a".repeat(40));
        config.set("companions.resource-pack.required", true);
        config.set("companions.resource-pack.prompt", "Visuals");

        PaperResourcePackDeliverySettings settings =
                PaperResourcePackDeliverySettingsLoader.load(config);

        assertTrue(settings.enabled());
        assertTrue(settings.required());
        assertEquals("a".repeat(40), settings.sha1());
    }

    @Test
    void enabledRejeitaUrlOuSha1Invalidos() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PaperResourcePackDeliverySettings(
                        true,
                        "file:///tmp/pack.zip",
                        "a".repeat(40),
                        false,
                        "Visuals"
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new PaperResourcePackDeliverySettings(
                        true,
                        "https://example.invalid/pack.zip",
                        "sha-invalido",
                        false,
                        "Visuals"
                )
        );
    }
}
