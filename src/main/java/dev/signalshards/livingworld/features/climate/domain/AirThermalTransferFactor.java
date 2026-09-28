package dev.signalshards.livingworld.features.climate.domain;

/**
 * Intensidade relativa da troca térmica com o ar/microclima.
 *
 * <p>1 representa exposição-base. Valores menores podem representar abrigo
 * ou isolamento espacial; valores maiores poderão representar vento/exposição.
 * O cálculo desses fatores pertence a slices posteriores.</p>
 */
public record AirThermalTransferFactor(double value) {
    public AirThermalTransferFactor {
        if (!Double.isFinite(value) || value < 0.0D) {
            throw new IllegalArgumentException(
                    "O fator de transferência do ar deve ser finito e não negativo"
            );
        }
    }

    public static AirThermalTransferFactor baseline() {
        return new AirThermalTransferFactor(1.0D);
    }

    public static AirThermalTransferFactor none() {
        return new AirThermalTransferFactor(0.0D);
    }
}
