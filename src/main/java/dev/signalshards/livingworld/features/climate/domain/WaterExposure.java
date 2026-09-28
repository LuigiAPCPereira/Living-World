package dev.signalshards.livingworld.features.climate.domain;

/**
 * Contato corporal do jogador com água.
 *
 * <p>{@code submergedFraction} representa a fração aproximada do corpo em
 * contato/submersa, de 0 (sem contato) a 1 (totalmente submerso).
 * {@code depthBlocks} representa a profundidade abaixo da superfície usada
 * apenas para intensificar transferência em água profunda; não representa
 * pressão nem causa dano por si só.</p>
 */
public record WaterExposure(double submergedFraction, double depthBlocks) {
    public WaterExposure {
        if (!Double.isFinite(submergedFraction)
                || submergedFraction < 0.0D
                || submergedFraction > 1.0D) {
            throw new IllegalArgumentException(
                    "A fração submersa deve ser finita e ficar entre 0 e 1"
            );
        }
        if (!Double.isFinite(depthBlocks) || depthBlocks < 0.0D) {
            throw new IllegalArgumentException(
                    "A profundidade da água deve ser finita e não negativa"
            );
        }
        if (submergedFraction == 0.0D && depthBlocks != 0.0D) {
            throw new IllegalArgumentException(
                    "Sem contato com água, a profundidade deve ser zero"
            );
        }
    }

    public static WaterExposure none() {
        return new WaterExposure(0.0D, 0.0D);
    }
}
