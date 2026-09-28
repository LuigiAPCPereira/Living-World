package dev.signalshards.livingworld.features.climate.domain;

import java.time.Duration;
import java.util.Objects;

/**
 * Integra molhamento/secagem ao longo do tempo sem conhecer a origem da taxa.
 */
public final class WetnessPolicy {
    public WetnessState advance(
            WetnessState current,
            WetnessRate rate,
            Duration elapsed
    ) {
        Objects.requireNonNull(current, "umidade atual");
        Objects.requireNonNull(rate, "taxa de umidade");
        Objects.requireNonNull(elapsed, "tempo decorrido");
        if (elapsed.isNegative()) {
            throw new IllegalArgumentException("O tempo decorrido não pode ser negativo");
        }

        double seconds = elapsed.getSeconds() + (elapsed.getNano() / 1_000_000_000.0D);
        double delta = rate.levelPerSecond() * seconds;
        if (!Double.isFinite(delta)) {
            throw new IllegalArgumentException("A integração de umidade excedeu um valor finito");
        }
        return current.adjustedBy(delta);
    }
}
