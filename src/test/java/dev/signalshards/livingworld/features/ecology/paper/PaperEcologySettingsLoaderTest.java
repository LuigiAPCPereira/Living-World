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
        config.set("ecology.ground-cover-spread.enabled", false);
        config.set("ecology.ground-cover-spread.strength", 0.30D);
        config.set("ecology.farmland-moisture-retention.enabled", false);
        config.set("ecology.farmland-moisture-retention.strength", 0.40D);
        config.set("ecology.fire-spread.enabled", false);
        config.set("ecology.fire-spread.strength", 0.45D);
        config.set("ecology.frozen-surfaces.enabled", false);

        PaperEcologySettings settings = PaperEcologySettingsLoader.load(config);

        assertFalse(settings.naturalGrowthEnabled());
        assertEquals(0.25D, settings.cropGrowthStrength());
        assertEquals(0.15D, settings.treeGrowthStrength());
        assertFalse(settings.groundCoverSpreadEnabled());
        assertEquals(0.30D, settings.groundCoverSpreadStrength());
        assertFalse(settings.farmlandMoistureRetentionEnabled());
        assertEquals(0.40D, settings.farmlandMoistureRetentionStrength());
        assertFalse(settings.fireSpreadEnabled());
        assertEquals(0.45D, settings.fireSpreadStrength());
        assertFalse(settings.frozenSurfacesEnabled());
    }

    @Test
    void aceitaChaveLegadaStrengthComoFallbackDePlantacao() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("ecology.natural-growth.strength", 0.40D);

        PaperEcologySettings settings = PaperEcologySettingsLoader.load(config);

        assertEquals(0.40D, settings.cropGrowthStrength());
        assertEquals(0.35D, settings.treeGrowthStrength());
        assertEquals(true, settings.groundCoverSpreadEnabled());
        assertEquals(0.50D, settings.groundCoverSpreadStrength());
        assertEquals(true, settings.farmlandMoistureRetentionEnabled());
        assertEquals(0.70D, settings.farmlandMoistureRetentionStrength());
        assertEquals(true, settings.fireSpreadEnabled());
        assertEquals(0.65D, settings.fireSpreadStrength());
        assertEquals(true, settings.frozenSurfacesEnabled());
    }

    @Test
    void rejeitaForcaInvalida() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("ecology.fire-spread.strength", 2.0D);

        assertThrows(
                IllegalArgumentException.class,
                () -> PaperEcologySettingsLoader.load(config)
        );
    }
}
