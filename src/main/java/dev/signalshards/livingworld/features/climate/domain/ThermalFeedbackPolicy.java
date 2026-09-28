package dev.signalshards.livingworld.features.climate.domain;

import java.time.Duration;
import java.util.Objects;

/**
 * Decide intensidade/cadência abstrata de feedback térmico.
 *
 * <p>Respiração depende do ar frio; frost depende do estado corporal acumulado.
 * Nenhum caminho desta policy aplica freeze ticks, dano, som ou partícula.</p>
 */
public final class ThermalFeedbackPolicy {
    private final ThermalFeedbackSettings settings;

    public ThermalFeedbackPolicy() {
        this(ThermalFeedbackSettings.livingWorldDefaults());
    }

    public ThermalFeedbackPolicy(ThermalFeedbackSettings settings) {
        this.settings = Objects.requireNonNull(
                settings,
                "configuração de feedback térmico"
        );
    }

    public ThermalFeedbackProfile profileFor(
            PlayerThermalState thermalState,
            AmbientTemperature ambientTemperature,
            PlayerActivity activity,
            WaterExposure waterExposure
    ) {
        Objects.requireNonNull(thermalState, "estado térmico");
        Objects.requireNonNull(ambientTemperature, "temperatura ambiente");
        Objects.requireNonNull(activity, "atividade");
        Objects.requireNonNull(waterExposure, "exposição à água");

        return new ThermalFeedbackProfile(
                breathFor(ambientTemperature, activity, waterExposure),
                frostIntensity(thermalState)
        );
    }

    private BreathFeedback breathFor(
            AmbientTemperature ambientTemperature,
            PlayerActivity activity,
            WaterExposure waterExposure
    ) {
        if (waterExposure.submergedFraction() >= 1.0D) {
            return BreathFeedback.none();
        }

        double intensity = Math.clamp(
                (settings.breathStartCelsius()
                        - ambientTemperature.degreesCelsius())
                        / (settings.breathStartCelsius()
                        - settings.breathFullCelsius()),
                0.0D,
                1.0D
        );
        if (intensity == 0.0D) {
            return BreathFeedback.none();
        }

        double activityMultiplier = switch (activity) {
            case RESTING, GLIDING -> 1.0D;
            case WALKING -> 0.90D;
            case SPRINTING -> 0.65D;
            case SWIMMING, CLIMBING -> 0.75D;
        };
        Duration minimum = scaledInterval(
                interpolateDuration(
                        settings.mildBreathMinimumInterval(),
                        settings.severeBreathMinimumInterval(),
                        intensity
                ),
                activityMultiplier
        );
        Duration maximum = scaledInterval(
                interpolateDuration(
                        settings.mildBreathMaximumInterval(),
                        settings.severeBreathMaximumInterval(),
                        intensity
                ),
                activityMultiplier
        );
        return new BreathFeedback(intensity, minimum, maximum);
    }

    private double frostIntensity(PlayerThermalState thermalState) {
        return Math.clamp(
                (settings.frostStartLoad() - thermalState.thermalLoad())
                        / (settings.frostStartLoad() - settings.frostFullLoad()),
                0.0D,
                1.0D
        );
    }

    private Duration interpolateDuration(
            Duration mild,
            Duration severe,
            double intensity
    ) {
        double nanos = mild.toNanos()
                + ((severe.toNanos() - mild.toNanos()) * intensity);
        return Duration.ofNanos(Math.max(1L, Math.round(nanos)));
    }

    private Duration scaledInterval(Duration interval, double multiplier) {
        return Duration.ofNanos(Math.max(
                1L,
                Math.round(interval.toNanos() * multiplier)
        ));
    }
}
