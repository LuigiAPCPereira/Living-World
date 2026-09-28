package dev.signalshards.livingworld.features.climate.domain;

/**
 * Temperatura de gameplay da água líquida em graus Celsius.
 *
 * <p>É independente da temperatura do ar e do estado corporal do jogador.
 * O valor continua sendo uma aproximação de gameplay, não uma simulação
 * hidrológica completa.</p>
 */
public record WaterTemperature(double degreesCelsius) {
    public WaterTemperature {
        if (!Double.isFinite(degreesCelsius)) {
            throw new IllegalArgumentException("A temperatura da água deve ser finita");
        }
    }
}
