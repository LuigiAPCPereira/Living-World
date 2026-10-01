package dev.signalshards.livingworld.features.hud.paper;

import org.bukkit.configuration.ConfigurationSection;

import java.util.Objects;

public final class PaperHudSettingsLoader {
    private PaperHudSettingsLoader() {
    }

    public static PaperHudSettings load(ConfigurationSection config) {
        Objects.requireNonNull(config, "configuração");
        PaperHudSettings defaults = PaperHudSettings.defaults();

        return new PaperHudSettings(
                config.getBoolean("hud.enabled", defaults.enabled()),
                config.getLong("hud.update-period-ticks", defaults.updatePeriodTicks()),
                config.getBoolean(
                        "hud.calendar-boss-bar.enabled",
                        defaults.calendarBossBarEnabled()
                ),
                config.getBoolean(
                        "hud.action-bar.navigation",
                        defaults.navigationEnabled()
                ),
                config.getBoolean(
                        "hud.action-bar.coordinates",
                        defaults.coordinatesEnabled()
                ),
                config.getBoolean(
                        "hud.action-bar.temperature",
                        defaults.temperatureEnabled()
                ),
                config.getBoolean(
                        "hud.action-bar.thermal-context.enabled",
                        defaults.thermalContextEnabled()
                ),
                config.getDouble(
                        "hud.action-bar.thermal-context.wetness-minimum",
                        defaults.wetnessMinimumDisplay()
                )
        );
    }
}
