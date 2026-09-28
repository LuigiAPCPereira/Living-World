package dev.signalshards.livingworld.features.climate.domain;

/**
 * Intensidade relativa da transferência térmica causada por contato com água.
 *
 * <p>Zero significa nenhuma transferência adicional por água. O valor é
 * adimensional e deliberadamente não possui sinal: a direção de ganho/perda
 * de calor dependerá de {@code WaterTemperature} em um slice posterior.</p>
 */
public record WaterThermalTransferFactor(double value) {
    public WaterThermalTransferFactor {
        if (!Double.isFinite(value) || value < 0.0D) {
            throw new IllegalArgumentException(
                    "O fator de transferência da água deve ser finito e não negativo"
            );
        }
    }

    public static WaterThermalTransferFactor neutral() {
        return new WaterThermalTransferFactor(0.0D);
    }
}
