package dev.signalshards.livingworld.core.companion.paper;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaperDatapackContractSettingsLoaderTest {
    @Test
    void defaultEhDesligado() {
        PaperDatapackContractSettings settings =
                PaperDatapackContractSettingsLoader.load(
                        new YamlConfiguration()
                );

        assertFalse(settings.enabled());
        assertFalse(settings.required());
        assertEquals("", settings.paperName());
    }

    @Test
    void carregaNomePaperERequired() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("companions.datapack.enabled", true);
        config.set("companions.datapack.paper-name", "file/living-world");
        config.set("companions.datapack.required", true);

        PaperDatapackContractSettings settings =
                PaperDatapackContractSettingsLoader.load(config);

        assertTrue(settings.enabled());
        assertTrue(settings.required());
        assertEquals("file/living-world", settings.paperName());
    }

    @Test
    void enabledExigeNomePaper() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PaperDatapackContractSettings(true, " ", false)
        );
    }
}
