package dev.signalshards.livingworld.features.climate.domain;

/**
 * Temperatura ambiental de gameplay do Living World em graus Celsius.
 *
 * <p>Este valor descreve o ambiente, não a sensação térmica nem o estado
 * corporal de um jogador. Fontes pessoais de exposição como água, fogo,
 * lava, armadura ou atividade não pertencem a este tipo.</p>
 */
public record AmbientTemperature(double degreesCelsius) {
    public AmbientTemperature {
        if (!Double.isFinite(degreesCelsius)) {
            throw new IllegalArgumentException("A temperatura ambiente deve ser finita");
        }
    }
}
