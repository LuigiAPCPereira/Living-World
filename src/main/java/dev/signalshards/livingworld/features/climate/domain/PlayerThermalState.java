package dev.signalshards.livingworld.features.climate.domain;

/**
 * Estado térmico corporal acumulado do jogador.
 *
 * <p>O índice é normalizado em [-1, +1]. Valores negativos indicam
 * déficit térmico, zero representa equilíbrio e valores positivos indicam
 * excesso térmico. A unidade é interna do Living World e não é Celsius.</p>
 */
public record PlayerThermalState(double thermalLoad) {
    public static final double MIN_LOAD = -1.0D;
    public static final double MAX_LOAD = 1.0D;

    public PlayerThermalState {
        if (!Double.isFinite(thermalLoad)) {
            throw new IllegalArgumentException("A carga térmica corporal deve ser finita");
        }
        if (thermalLoad < MIN_LOAD || thermalLoad > MAX_LOAD) {
            throw new IllegalArgumentException(
                    "A carga térmica corporal deve ficar entre -1 e 1"
            );
        }
    }

    public static PlayerThermalState neutral() {
        return new PlayerThermalState(0.0D);
    }

    public PlayerThermalState adjustedBy(double delta) {
        if (!Double.isFinite(delta)) {
            throw new IllegalArgumentException("A variação térmica deve ser finita");
        }
        return new PlayerThermalState(
                Math.clamp(thermalLoad + delta, MIN_LOAD, MAX_LOAD)
        );
    }
}
