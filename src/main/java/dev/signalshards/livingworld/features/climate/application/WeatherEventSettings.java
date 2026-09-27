package dev.signalshards.livingworld.features.climate.application;

import java.time.Duration;
import java.util.Objects;

public record WeatherEventSettings(
        Duration clearDuration,
        Duration precipitationDuration,
        Duration stormDuration
) {
    private static final Duration MAX_DURATION = Duration.ofMinutes(30);

    public WeatherEventSettings {
        validateDuration(clearDuration, "tempo limpo");
        validateDuration(precipitationDuration, "precipitação");
        validateDuration(stormDuration, "tempestade");
    }

    public static WeatherEventSettings defaults() {
        return new WeatherEventSettings(
                Duration.ofMinutes(10),
                Duration.ofMinutes(5),
                Duration.ofMinutes(3)
        );
    }

    private static void validateDuration(Duration duration, String eventName) {
        Objects.requireNonNull(duration, "duração de " + eventName);
        if (duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException("A duração de " + eventName + " deve ser positiva");
        }
        if (duration.compareTo(MAX_DURATION) > 0) {
            throw new IllegalArgumentException("A duração de " + eventName + " não pode ultrapassar 30 minutos");
        }
    }
}
