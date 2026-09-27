package dev.signalshards.livingworld.features.ecology.paper;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaperEcologySettingsLoaderTest {
    @Test
    void usaDefaultsModerados() {
        assertEquals(
                PaperEcologySettings.defaults(),
                PaperEcologySettingsLoader.load(new YamlConfiguration())
        );
    }

    @Test
    void permiteDesligarEAjustarForca() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("ecology.natural-growth.enabled", false);
        config.set("ecology.natural-growth.crop-strength", 0.25D);
        config.set("ecology.natural-growth.tree-strength", 0.15D);
        config.set("ecology.frozen-surfaces.enabled", false);

        PaperEcologySettings settings = PaperEcologySettingsLoader.load(config);

        assertFalse(settings.naturalGrowthEnabled());
        assertEquals(0.25D, settings.cropGrowthStrength());
        assertEquals(0.15D, settings.treeGrowthStrength());
        assertFalse(settings.frozenSurfacesEnabled());
    }

    @Test
    void aceitaChaveLegadaStrengthComoFallbackDePlantacao() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("ecology.natural-growth.strength", 0.40D);

        PaperEcologySettings settings = PaperEcologySettingsLoader.load(config);

        assertEquals(0.40D, settings.cropGrowthStrength());
        assertEquals(0.35D, settings.treeGrowthStrength());
        assertEquals(true, settings.frozenSurfacesEnabled());
    }

    @Test
    void rejeitaForcaInvalida() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("ecology.natural-growth.tree-strength", 2.0D);

        assertThrows(
                IllegalArgumentException.class,
                () -> PaperEcologySettingsLoader.load(config)
        );
    }
}
