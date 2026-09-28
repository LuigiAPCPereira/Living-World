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

    public AmbientTemperature temperature(double paperTemperature, Season season) {
        if (!Double.isFinite(paperTemperature)) {
            throw new IllegalArgumentException("A temperatura do Paper deve ser finita");
        }
        Objects.requireNonNull(season, "estação");

        double base = NEUTRAL_CELSIUS
                + ((paperTemperature - NEUTRAL_PAPER_TEMPERATURE)
                * CELSIUS_PER_PAPER_UNIT);
        double seasonalAdjustment = switch (season) {
            case PRIMAVERA -> 2.0D;
            case VERAO -> 6.0D;
            case OUTONO -> 0.0D;
            case INVERNO -> -6.0D;
        };

        return new AmbientTemperature(base + seasonalAdjustment);
    }
}
