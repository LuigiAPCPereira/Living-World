package dev.signalshards.livingworld.features.desirelines.paper;

import dev.signalshards.livingworld.features.desirelines.domain.PathWearSettings;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Objects;

public final class PaperPathWearSettingsLoader {
    private PaperPathWearSettingsLoader() {
    }

    public static PathWearSettings load(ConfigurationSection config) {
        Objects.requireNonNull(config, "configuração");
        PathWearSettings defaults = PathWearSettings.defaults();

        return new PathWearSettings(
                config.getBoolean("desire-lines.enabled", defaults.enabled()),
                config.getInt(
                        "desire-lines.grass-to-dirt-visits",
                        defaults.grassToDirtVisits()
                ),
                config.getInt(
                        "desire-lines.dirt-to-path-visits",
                        defaults.dirtToPathVisits()
                ),
                config.getInt(
                        "desire-lines.max-tracked-positions-per-chunk",
                        defaults.maxTrackedPositionsPerChunk()
                ),
                config.getLong(
                        "desire-lines.flush-period-ticks",
                        defaults.flushPeriodTicks()
                )
        );
    }
}
