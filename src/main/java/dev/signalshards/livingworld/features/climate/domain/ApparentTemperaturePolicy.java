package dev.signalshards.livingworld.features.climate.domain;

import dev.signalshards.livingworld.features.seasons.domain.Season;

import java.util.Objects;

/**
 * Converte a temperatura escalar exposta pelo mundo em uma escala de gameplay
 * expressa em graus Celsius para apresentação do Living World.
 *
 * <p>Não é uma conversão oficial do Minecraft. O ponto neutro inicial é
 * 0,8 -> 15 °C, cada unidade do escalar vale 20 °C e a estação aplica um
 * ajuste moderado. O resultado é limitado para manter o HUD legível.</p>
 */
public final class ApparentTemperaturePolicy {
    private static final double NEUTRAL_PAPER_TEMPERATURE = 0.8D;
    private static final double NEUTRAL_CELSIUS = 15.0D;
    private static final double CELSIUS_PER_PAPER_UNIT = 20.0D;
    private static final int MIN_CELSIUS = -40;
    private static final int MAX_CELSIUS = 55;

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

        double base = NEUTRAL_CELSIUS
                + ((paperTemperature - NEUTRAL_PAPER_TEMPERATURE)
                * CELSIUS_PER_PAPER_UNIT);
        double seasonalAdjustment = switch (season) {
            case PRIMAVERA -> 2.0D;
            case VERAO -> 6.0D;
            case OUTONO -> 0.0D;
            case INVERNO -> -6.0D;
        };
        double exposureAdjustment = switch (exposure) {
            case NONE -> 0.0D;
            case WATER -> -4.0D;
            case FIRE -> 8.0D;
            case LAVA -> 20.0D;
        };

        long rounded = Math.round(
                base + seasonalAdjustment + exposureAdjustment
        );
        return (int) Math.max(MIN_CELSIUS, Math.min(MAX_CELSIUS, rounded));
    }
}
