package dev.signalshards.livingworld.features.climate.domain;

/**
 * Potência e alcance térmico de gameplay de uma fonte local.
 */
public record LocalHeatSourceProfile(
        double peakLoadPerSecond,
        double effectiveRangeBlocks
) {
    public LocalHeatSourceProfile {
        if (!Double.isFinite(peakLoadPerSecond) || peakLoadPerSecond < 0.0D) {
            throw new IllegalArgumentException("A potência térmica deve ser finita e não negativa");
        }
        if (!Double.isFinite(effectiveRangeBlocks) || effectiveRangeBlocks <= 0.0D) {
            throw new IllegalArgumentException("O alcance térmico deve ser positivo");
        }
    }
}
