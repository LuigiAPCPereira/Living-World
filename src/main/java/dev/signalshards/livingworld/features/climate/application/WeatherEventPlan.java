package dev.signalshards.livingworld.features.climate.application;

import dev.signalshards.livingworld.features.climate.domain.WeatherTendency;

import java.time.Duration;
import java.util.Objects;

public record WeatherEventPlan(WeatherTendency tendency, Duration duration) {
    public WeatherEventPlan {
        Objects.requireNonNull(tendency, "tendência climática");
        Objects.requireNonNull(duration, "duração");
    }
}
