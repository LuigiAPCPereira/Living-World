package dev.signalshards.livingworld.features.hud.paper;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaperHudSettingsLoaderTest {
    @Test
    void usaDefaultsDoHud() {
        assertEquals(
                PaperHudSettings.defaults(),
                PaperHudSettingsLoader.load(new YamlConfiguration())
        );
    }

    @Test
    void permiteDesligarSuperficiesIndependentemente() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("hud.action-bar.navigation", false);
        config.set("hud.action-bar.coordinates", false);
        config.set("hud.action-bar.temperature", false);

        PaperHudSettings settings = PaperHudSettingsLoader.load(config);

        assertFalse(settings.actionBarEnabled());
    }

    @Test
    void rejeitaCadenciaExcessiva() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("hud.update-period-ticks", 1);

        assertThrows(
                IllegalArgumentException.class,
                () -> PaperHudSettingsLoader.load(config)
        );
    }
}
