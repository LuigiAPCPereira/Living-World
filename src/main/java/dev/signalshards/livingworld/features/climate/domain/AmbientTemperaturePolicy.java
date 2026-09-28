package dev.signalshards.livingworld.features.climate.domain;

import dev.signalshards.livingworld.features.seasons.domain.Season;

import java.util.Objects;

/**
 * Converte o escalar térmico local exposto pelo Paper em temperatura
 * ambiental de gameplay do Living World.
 *
 * <p>Esta primeira fatia preserva a calibração já validada pelo HUD:
 * 0,8 -> 15 °C antes do ajuste sazonal e 20 °C por unidade do escalar.
 * A política não aplica exposição pessoal e não usa o clamp de apresentação
 * do HUD; esses limites pertencem ao consumidor, não ao ambiente.</p>
 */
public final class AmbientTemperaturePolicy {
    private static final double NEUTRAL_PAPER_TEMPERATURE = 0.8D;
    private static final double NEUTRAL_CELSIUS = 15.0D;
    private static final double CELSIUS_PER_PAPER_UNIT = 20.0D;
    private final AmbientTemperatureSettings settings;

    public AmbientTemperaturePolicy() {
        this(AmbientTemperatureSettings.livingWorldDefaults());
    }

    public AmbientTemperaturePolicy(AmbientTemperatureSettings settings) {
        this.settings = Objects.requireNonNull(
                settings,
                "configuração de temperatura ambiente"
        );
    }

    public AmbientTemperature temperature(double paperTemperature, Season season) {
        if (!Double.isFinite(paperTemperature)) {
            throw new IllegalArgumentException("A temperatura do Paper deve ser finita");
        }
        Objects.requireNonNull(season, "estação");

        return new AmbientTemperature(baseTemperature(paperTemperature, season));
    }

    public AmbientTemperature temperature(
            double paperTemperature,
            Season season,
            AmbientTemperatureFactors factors
    ) {
        if (!Double.isFinite(paperTemperature)) {
            throw new IllegalArgumentException("A temperatura do Paper deve ser finita");
        }
        Objects.requireNonNull(season, "estação");
        Objects.requireNonNull(factors, "fatores ambientais");

        double base = baseTemperature(paperTemperature, season);
        double temporal = temporalAdjustment(factors) * factors.skyExposure();
        double weather = weatherAdjustment(factors.weather()) * factors.skyExposure();
        return new AmbientTemperature(base + temporal + weather);
    }

    private double baseTemperature(double paperTemperature, Season season) {
        double base = NEUTRAL_CELSIUS
                + ((paperTemperature - NEUTRAL_PAPER_TEMPERATURE)
                * CELSIUS_PER_PAPER_UNIT);
        double seasonalAdjustment = switch (season) {
            case PRIMAVERA -> 2.0D;
            case VERAO -> 6.0D;
            case OUTONO -> 0.0D;
            case INVERNO -> -6.0D;
        };

        return base + seasonalAdjustment;
    }

    private double temporalAdjustment(AmbientTemperatureFactors factors) {
        double angle = ((factors.timeOfDayTicks() - 6_000) / 24_000.0D)
                * Math.PI
                * 2.0D;
        double daylight = Math.cos(angle);
        if (daylight >= 0.0D) {
            return daylight * settings.daytimeWarmingCelsius();
        }
        return daylight * settings.nighttimeCoolingCelsius();
    }

    private double weatherAdjustment(AmbientWeather weather) {
        return switch (weather) {
            case CLEAR -> 0.0D;
            case RAIN -> -settings.rainCoolingCelsius();
            case THUNDER -> -settings.thunderCoolingCelsius();
        };
    }
}
