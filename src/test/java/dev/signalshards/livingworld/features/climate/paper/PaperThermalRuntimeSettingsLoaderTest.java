package dev.signalshards.livingworld.features.climate.paper;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PaperThermalRuntimeSettingsLoaderTest {
    @Test
    void usaDefaultsQuandoNaoConfigurado() {
        assertEquals(
                PaperThermalRuntimeSettings.defaults(),
                PaperThermalRuntimeSettingsLoader.load(new YamlConfiguration())
        );
    }

    @Test
    void carregaToggleEPeriodo() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("climate.thermal-runtime.enabled", false);
        config.set("climate.thermal-runtime.update-period-ticks", 40L);

        PaperThermalRuntimeSettings settings =
                PaperThermalRuntimeSettingsLoader.load(config);

        assertFalse(settings.enabled());
        assertEquals(40L, settings.updatePeriodTicks());
    }
}
