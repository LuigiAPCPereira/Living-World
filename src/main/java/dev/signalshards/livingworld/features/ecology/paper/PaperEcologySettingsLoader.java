package dev.signalshards.livingworld.features.ecology.paper;

import org.bukkit.configuration.ConfigurationSection;

import java.util.Objects;

public final class PaperEcologySettingsLoader {
    private PaperEcologySettingsLoader() {
    }

    public static PaperEcologySettings load(ConfigurationSection config) {
        Objects.requireNonNull(config, "configuração");
        PaperEcologySettings defaults = PaperEcologySettings.defaults();
        double legacyStrength = config.getDouble(
                "ecology.natural-growth.strength",
                defaults.cropGrowthStrength()
        );

        return new PaperEcologySettings(
                config.getBoolean(
                        "ecology.natural-growth.enabled",
                        defaults.naturalGrowthEnabled()
                ),
                config.getDouble(
                        "ecology.natural-growth.crop-strength",
                        legacyStrength
                ),
                config.getDouble(
                        "ecology.natural-growth.tree-strength",
                        defaults.treeGrowthStrength()
                ),
                config.getBoolean(
                        "ecology.ground-cover-spread.enabled",
                        defaults.groundCoverSpreadEnabled()
                ),
                config.getDouble(
                        "ecology.ground-cover-spread.strength",
                        defaults.groundCoverSpreadStrength()
                ),
                config.getBoolean(
                        "ecology.farmland-moisture-retention.enabled",
                        defaults.farmlandMoistureRetentionEnabled()
                ),
                config.getDouble(
                        "ecology.farmland-moisture-retention.strength",
                        defaults.farmlandMoistureRetentionStrength()
                ),
                config.getBoolean(
                        "ecology.frozen-surfaces.enabled",
                        defaults.frozenSurfacesEnabled()
                )
        );
    }
}
