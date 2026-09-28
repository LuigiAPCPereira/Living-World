package dev.signalshards.livingworld.features.climate.domain;

import dev.signalshards.livingworld.features.seasons.domain.Season;

import java.util.Objects;

/**
 * Converte temperatura ambiente + exposição pessoal em uma temperatura
 * aparente de apresentação do Living World.
 *
 * <p>A calibração ambiental pertence a {@link AmbientTemperaturePolicy}.
 * Esta política mantém os modificadores pessoais legados e o clamp do HUD
 * durante a migração para o modelo térmico corporal. A escala continua sendo
 * uma convenção de gameplay do Living World, não uma conversão oficial do
 * Minecraft.</p>
 */
public final class ApparentTemperaturePolicy {
    private static final int MIN_CELSIUS = -40;
    private static final int MAX_CELSIUS = 55;
    private final AmbientTemperaturePolicy ambientTemperaturePolicy;

    public ApparentTemperaturePolicy() {
        this(new AmbientTemperaturePolicy());
    }

    ApparentTemperaturePolicy(AmbientTemperaturePolicy ambientTemperaturePolicy) {
        this.ambientTemperaturePolicy = Objects.requireNonNull(
                ambientTemperaturePolicy,
                "política de temperatura ambiente"
        );
    }

    public int degreesCelsius(double paperTemperature, Season season) {
        return degreesCelsius(
                paperTemperature,
                season,
                TemperatureExposure.NONE
        );
    }

    public int degreesCelsius(
            double paperTemperature,
            Season season,
            TemperatureExposure exposure
    ) {
        if (!Double.isFinite(paperTemperature)) {
            throw new IllegalArgumentException("A temperatura do Paper deve ser finita");
        }
        Objects.requireNonNull(season, "estação");
        Objects.requireNonNull(exposure, "exposição térmica");

        AmbientTemperature ambientTemperature = ambientTemperaturePolicy.temperature(
                paperTemperature,
                season
        );
        return degreesCelsius(ambientTemperature, exposure);
    }

    public int degreesCelsius(
            AmbientTemperature ambientTemperature,
            TemperatureExposure exposure
    ) {
        Objects.requireNonNull(ambientTemperature, "temperatura ambiente");
        Objects.requireNonNull(exposure, "exposição térmica");
        double exposureAdjustment = switch (exposure) {
            case NONE -> 0.0D;
            case WATER -> -4.0D;
            case FIRE -> 8.0D;
            case LAVA -> 20.0D;
        };

        long rounded = Math.round(
                ambientTemperature.degreesCelsius() + exposureAdjustment
        );
        return Math.clamp(rounded, MIN_CELSIUS, MAX_CELSIUS);
    }
}
