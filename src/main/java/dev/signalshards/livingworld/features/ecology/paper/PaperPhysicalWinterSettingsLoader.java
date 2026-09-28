package dev.signalshards.livingworld.features.ecology.paper;

import dev.signalshards.livingworld.features.ecology.domain.PhysicalWinterSettings;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Objects;

public final class PaperPhysicalWinterSettingsLoader {
    private PaperPhysicalWinterSettingsLoader() {
    }

    public static PaperPhysicalWinterSettings load(
            ConfigurationSection config
    ) {
        Objects.requireNonNull(config, "configuração");
        PaperPhysicalWinterSettings defaults =
                PaperPhysicalWinterSettings.defaults();
        PhysicalWinterSettings domainDefaults = defaults.domain();

        return new PaperPhysicalWinterSettings(
                config.getBoolean(
                        "ecology.physical-winter.enabled",
                        defaults.enabled()
                ),
                config.getLong(
                        "ecology.physical-winter.update-period-ticks",
                        defaults.updatePeriodTicks()
                ),
                config.getInt(
                        "ecology.physical-winter.probes-per-player",
                        defaults.probesPerPlayer()
                ),
                config.getInt(
                        "ecology.physical-winter.max-mutations-per-player",
                        defaults.maxMutationsPerPlayer()
                ),
                config.getInt(
                        "ecology.physical-winter.radius-blocks",
                        defaults.radiusBlocks()
                ),
                new PhysicalWinterSettings(
                        config.getDouble(
                                "ecology.physical-winter.freeze-at-or-below-celsius",
                                domainDefaults.freezeAtOrBelowCelsius()
                        ),
                        config.getDouble(
                                "ecology.physical-winter.thaw-at-or-above-celsius",
                                domainDefaults.thawAtOrAboveCelsius()
                        ),
                        config.getInt(
                                "ecology.physical-winter.max-owned-positions-per-chunk",
                                domainDefaults.maxOwnedPositionsPerChunk()
                        )
                )
        );
    }
}
