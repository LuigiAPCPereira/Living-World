package dev.signalshards.livingworld.features.climate.application;

import dev.signalshards.livingworld.features.climate.domain.ClimateSnapshot;

import java.util.Objects;
import java.util.Optional;

public final class WeatherEventPlanner {
    private final WeatherEventSettings settings;

    public WeatherEventPlanner(WeatherEventSettings settings) {
        this.settings = Objects.requireNonNull(settings, "configuração de eventos climáticos");
    }

    public Optional<WeatherEventPlan> plan(ClimateSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "fotografia climática");

        return switch (snapshot.weatherTendency()) {
            case ESTAVEL -> Optional.empty();
            case TEMPO_LIMPO -> Optional.of(
                    new WeatherEventPlan(snapshot.weatherTendency(), settings.clearDuration())
            );
            case PRECIPITACAO -> Optional.of(
                    new WeatherEventPlan(snapshot.weatherTendency(), settings.precipitationDuration())
            );
            case TEMPESTADE -> Optional.of(
                    new WeatherEventPlan(snapshot.weatherTendency(), settings.stormDuration())
            );
        };
    }
}
