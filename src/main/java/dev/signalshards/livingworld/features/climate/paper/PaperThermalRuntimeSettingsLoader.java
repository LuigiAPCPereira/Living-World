package dev.signalshards.livingworld.features.climate.paper;

import org.bukkit.configuration.ConfigurationSection;

import java.util.Objects;

public final class PaperThermalRuntimeSettingsLoader {
    private PaperThermalRuntimeSettingsLoader() {
    }

    public static PaperThermalRuntimeSettings load(ConfigurationSection config) {
        Objects.requireNonNull(config, "configuração");
        PaperThermalRuntimeSettings defaults = PaperThermalRuntimeSettings.defaults();
        return new PaperThermalRuntimeSettings(
                config.getBoolean(
                        "climate.thermal-runtime.enabled",
                        defaults.enabled()
                ),
                config.getLong(
                        "climate.thermal-runtime.update-period-ticks",
                        defaults.updatePeriodTicks()
                )
        );
    }
}
