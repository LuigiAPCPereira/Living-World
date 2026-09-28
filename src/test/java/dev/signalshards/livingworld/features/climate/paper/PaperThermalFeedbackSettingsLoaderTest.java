package dev.signalshards.livingworld.features.climate.paper;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaperThermalFeedbackSettingsLoaderTest {
    @Test
    void usaDefaultsQuandoNaoConfigurado() {
        assertEquals(
                PaperThermalFeedbackSettings.defaults(),
                PaperThermalFeedbackSettingsLoader.load(new YamlConfiguration())
        );
    }

    @Test
    void carregaToggleEPeriodo() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("climate.thermal-feedback.enabled", false);
        config.set("climate.thermal-feedback.update-period-ticks", 5L);
        config.set("climate.thermal-feedback.cold-breath.enabled", false);
        config.set("climate.thermal-feedback.frost.enabled", true);

        PaperThermalFeedbackSettings settings =
                PaperThermalFeedbackSettingsLoader.load(config);

        assertFalse(settings.enabled());
        assertEquals(5L, settings.updatePeriodTicks());
        assertFalse(settings.coldBreathEnabled());
        assertTrue(settings.frostEnabled());
    }
}
