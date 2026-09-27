package dev.signalshards.livingworld.features.climate.paper;

import dev.signalshards.livingworld.features.climate.application.WeatherEventSettings;
import org.bukkit.configuration.ConfigurationSection;

import java.time.Duration;
import java.util.Objects;

public final class PaperWeatherEventSettingsLoader {
    private static final long DEFAULT_CLEAR_SECONDS = 600L;
    private static final long DEFAULT_PRECIPITATION_SECONDS = 300L;
    private static final long DEFAULT_STORM_SECONDS = 180L;

    private PaperWeatherEventSettingsLoader() {
    }

    public static WeatherEventSettings load(ConfigurationSection config) {
        Objects.requireNonNull(config, "configuração");

        return new WeatherEventSettings(
                Duration.ofSeconds(config.getLong(
                        "climate.weather-events.clear-duration-seconds",
                        DEFAULT_CLEAR_SECONDS
                )),
                Duration.ofSeconds(config.getLong(
                        "climate.weather-events.precipitation-duration-seconds",
                        DEFAULT_PRECIPITATION_SECONDS
                )),
                Duration.ofSeconds(config.getLong(
                        "climate.weather-events.storm-duration-seconds",
                        DEFAULT_STORM_SECONDS
                ))
        );
    }
}
