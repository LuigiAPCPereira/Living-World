package dev.signalshards.livingworld.features.climate.domain;

/**
 * Taxa líquida de troca térmica corporal por segundo.
 *
 * <p>Valor positivo aquece, negativo esfria e zero mantém a carga atual.
 * Microclima, água, vento, atividade, isolamento e fontes locais irão
 * compor esta taxa em slices posteriores.</p>
 */
public record ThermalExchangeRate(double loadPerSecond) {
    public ThermalExchangeRate {
        if (!Double.isFinite(loadPerSecond)) {
            throw new IllegalArgumentException("A taxa de troca térmica deve ser finita");
        }
    }

    public static ThermalExchangeRate neutral() {
        return new ThermalExchangeRate(0.0D);
    }
}
