package dev.signalshards.livingworld.features.climate.paper;

import org.bukkit.configuration.ConfigurationSection;

import java.util.Objects;

public final class PaperThermalFeedbackSettingsLoader {
    private PaperThermalFeedbackSettingsLoader() {
    }

    public static PaperThermalFeedbackSettings load(ConfigurationSection config) {
        Objects.requireNonNull(config, "configuração");
        PaperThermalFeedbackSettings defaults = PaperThermalFeedbackSettings.defaults();
        return new PaperThermalFeedbackSettings(
                config.getBoolean(
                        "climate.thermal-feedback.enabled",
                        defaults.enabled()
                ),
                config.getLong(
                        "climate.thermal-feedback.update-period-ticks",
                        defaults.updatePeriodTicks()
                ),
                config.getBoolean(
                        "climate.thermal-feedback.cold-breath.enabled",
                        defaults.coldBreathEnabled()
                ),
                config.getBoolean(
                        "climate.thermal-feedback.frost.enabled",
                        defaults.frostEnabled()
                )
        );
    }
}
