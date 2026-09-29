package dev.signalshards.livingworld.features.ecology.paper;

import dev.signalshards.livingworld.features.ecology.domain.PhysicalSnowSettings;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Objects;

public final class PaperPhysicalSnowSettingsLoader {
    private PaperPhysicalSnowSettingsLoader() {
    }

    public static PhysicalSnowSettings load(ConfigurationSection config) {
        Objects.requireNonNull(config, "configuração");
        PhysicalSnowSettings defaults = PhysicalSnowSettings.defaults();
        return new PhysicalSnowSettings(
                config.getInt(
                        "ecology.physical-winter.max-snow-layers",
                        defaults.maxLayers()
                )
        );
    }
}
