package dev.signalshards.livingworld.features.climate.domain;

import java.time.Duration;
import java.util.Objects;

/**
 * Integra uma taxa líquida de troca térmica ao longo do tempo e classifica
 * o estado corporal resultante.
 *
 * <p>Esta política não sabe por que o jogador está aquecendo/esfriando.
 * Água, armadura, vento, atividade e fontes locais pertencem ao cálculo
 * anterior da {@link ThermalExchangeRate}.</p>
 */
public final class PlayerThermalPolicy {
    private final PlayerThermalThresholds thresholds;

    public PlayerThermalPolicy() {
        this(PlayerThermalThresholds.livingWorldDefaults());
    }

    public PlayerThermalPolicy(PlayerThermalThresholds thresholds) {
        this.thresholds = Objects.requireNonNull(thresholds, "limites térmicos");
    }

    public PlayerThermalState advance(
            PlayerThermalState current,
            ThermalExchangeRate exchangeRate,
            Duration elapsed
    ) {
        Objects.requireNonNull(current, "estado térmico atual");
        Objects.requireNonNull(exchangeRate, "taxa de troca térmica");
        Objects.requireNonNull(elapsed, "tempo decorrido");
        if (elapsed.isNegative()) {
            throw new IllegalArgumentException("O tempo decorrido não pode ser negativo");
        }

        double seconds = elapsed.getSeconds() + (elapsed.getNano() / 1_000_000_000.0D);
        double delta = exchangeRate.loadPerSecond() * seconds;
        if (!Double.isFinite(delta)) {
            throw new IllegalArgumentException("A integração térmica excedeu um valor finito");
        }
        return current.adjustedBy(delta);
    }

    public PlayerThermalBand bandFor(PlayerThermalState state) {
        Objects.requireNonNull(state, "estado térmico");
        double load = state.thermalLoad();
        if (load <= thresholds.extremeColdMax()) {
            return PlayerThermalBand.EXTREME_COLD;
        }
        if (load <= thresholds.freezingMax()) {
            return PlayerThermalBand.FREEZING;
        }
        if (load <= thresholds.veryColdMax()) {
            return PlayerThermalBand.VERY_COLD;
        }
        if (load <= thresholds.coldMax()) {
            return PlayerThermalBand.COLD;
        }
        if (load <= thresholds.coolMax()) {
            return PlayerThermalBand.COOL;
        }
        if (load <= thresholds.comfortableMax()) {
            return PlayerThermalBand.COMFORTABLE;
        }
        if (load <= thresholds.warmMax()) {
            return PlayerThermalBand.WARM;
        }
        if (load <= thresholds.hotMax()) {
            return PlayerThermalBand.HOT;
        }
        if (load <= thresholds.overheatingMax()) {
            return PlayerThermalBand.OVERHEATING;
        }
        return PlayerThermalBand.EXTREME_HEAT;
    }
}
