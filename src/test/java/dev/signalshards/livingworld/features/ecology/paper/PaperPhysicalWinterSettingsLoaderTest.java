package dev.signalshards.livingworld.features.ecology.paper;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaperPhysicalWinterSettingsLoaderTest {
    @Test
    void defaultsMantemMutacaoAtivaDesligada() {
        PaperPhysicalWinterSettings settings =
                PaperPhysicalWinterSettingsLoader.load(
                        new YamlConfiguration()
                );

        assertFalse(settings.enabled());
        assertEquals(40L, settings.updatePeriodTicks());
        assertEquals(4, settings.probesPerPlayer());
        assertEquals(1, settings.maxMutationsPerPlayer());
        assertEquals(8, settings.radiusBlocks());
    }

    @Test
    void carregaBudgetsEThresholds() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("ecology.physical-winter.enabled", true);
        config.set("ecology.physical-winter.update-period-ticks", 60L);
        config.set("ecology.physical-winter.probes-per-player", 6);
        config.set("ecology.physical-winter.max-mutations-per-player", 2);
        config.set("ecology.physical-winter.radius-blocks", 12);
        config.set(
                "ecology.physical-winter.freeze-at-or-below-celsius",
                -2.0D
        );
        config.set(
                "ecology.physical-winter.thaw-at-or-above-celsius",
                3.0D
        );

        PaperPhysicalWinterSettings settings =
                PaperPhysicalWinterSettingsLoader.load(config);

        assertTrue(settings.enabled());
        assertEquals(6, settings.probesPerPlayer());
        assertEquals(2, settings.maxMutationsPerPlayer());
        assertEquals(-2.0D, settings.domain().freezeAtOrBelowCelsius());
        assertEquals(3.0D, settings.domain().thawAtOrAboveCelsius());
    }

    @Test
    void rejeitaMutationBudgetMaiorQueProbes() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PaperPhysicalWinterSettings(
                        true,
                        40L,
                        2,
                        3,
                        8,
                        dev.signalshards.livingworld.features.ecology.domain
                                .PhysicalWinterSettings.defaults()
                )
        );
    }
}
