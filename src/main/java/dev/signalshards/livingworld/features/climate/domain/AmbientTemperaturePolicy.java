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
        return temperature(paperTemperature, season, 0.5D, factors);
    }

    public AmbientTemperature temperature(
            double paperTemperature,
            Season season,
            double seasonProgress,
            AmbientTemperatureFactors factors
    ) {
        return temperature(
                paperTemperature,
                season,
                seasonProgress,
                factors,
                new EnvironmentalDimensionPolicy().profileFor(
                        EnvironmentalDimension.OVERWORLD
                )
        );
    }

    public AmbientTemperature temperature(
            double paperTemperature,
            Season season,
            double seasonProgress,
            AmbientTemperatureFactors factors,
            EnvironmentalDimensionProfile dimensionProfile
    ) {
        if (!Double.isFinite(paperTemperature)) {
            throw new IllegalArgumentException("A temperatura do Paper deve ser finita");
        }
        Objects.requireNonNull(season, "estação");
        if (!Double.isFinite(seasonProgress)
                || seasonProgress < 0.0D
                || seasonProgress > 1.0D) {
            throw new IllegalArgumentException(
                    "O progresso sazonal deve ficar entre 0 e 1"
            );
        }
        Objects.requireNonNull(factors, "fatores ambientais");
        Objects.requireNonNull(dimensionProfile, "perfil de dimensão");

        double base = baseTemperatureWithoutSeason(paperTemperature)
                + (seasonalAdjustment(season, seasonProgress)
                * dimensionProfile.seasonalityFactor());
        double temporal = temporalAdjustment(factors)
                * factors.skyExposure()
                * dimensionProfile.dayNightFactor();
        double weather = weatherAdjustment(factors.weather())
                * factors.skyExposure()
                * dimensionProfile.weatherFactor();
        return new AmbientTemperature(base + temporal + weather);
    }

    private double baseTemperature(double paperTemperature, Season season) {
        return baseTemperatureWithoutSeason(paperTemperature)
                + seasonalAnchor(season);
    }

    private double baseTemperatureWithoutSeason(double paperTemperature) {
        return NEUTRAL_CELSIUS
                + ((paperTemperature - NEUTRAL_PAPER_TEMPERATURE)
                * CELSIUS_PER_PAPER_UNIT);
    }

    private double seasonalAnchor(Season season) {
        return switch (season) {
            case PRIMAVERA -> 2.0D;
            case VERAO -> 6.0D;
            case OUTONO -> 0.0D;
            case INVERNO -> -6.0D;
        };
    }

    private double seasonalAdjustment(Season season, double progress) {
        Season previous = switch (season) {
            case PRIMAVERA -> Season.INVERNO;
            case VERAO -> Season.PRIMAVERA;
            case OUTONO -> Season.VERAO;
            case INVERNO -> Season.OUTONO;
        };
        Season next = switch (season) {
            case PRIMAVERA -> Season.VERAO;
            case VERAO -> Season.OUTONO;
            case OUTONO -> Season.INVERNO;
            case INVERNO -> Season.PRIMAVERA;
        };
        double currentAnchor = seasonalAnchor(season);
        double previousBoundary = (
                seasonalAnchor(previous) + currentAnchor
        ) / 2.0D;
        double nextBoundary = (
                currentAnchor + seasonalAnchor(next)
        ) / 2.0D;

        if (progress <= 0.5D) {
            return interpolate(
                    previousBoundary,
                    currentAnchor,
                    smoothStep(progress * 2.0D)
            );
        }
        return interpolate(
                currentAnchor,
                nextBoundary,
                smoothStep((progress - 0.5D) * 2.0D)
        );
    }

    private double smoothStep(double value) {
        double clamped = Math.clamp(value, 0.0D, 1.0D);
        return clamped * clamped * (3.0D - (2.0D * clamped));
    }

    private double interpolate(double start, double end, double progress) {
        return start + ((end - start) * progress);
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
