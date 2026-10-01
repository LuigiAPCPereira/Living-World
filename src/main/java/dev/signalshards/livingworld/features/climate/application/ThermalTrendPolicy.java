package dev.signalshards.livingworld.features.climate.application;

/**
 * Projeta a taxa térmica líquida já calculada pelo runtime em uma tendência
 * legível. Não recalcula clima nem troca térmica.
 */
public final class ThermalTrendPolicy {
    static final double STABLE_RATE_PER_SECOND = 0.0005D;

    public ThermalTrend classify(double netRatePerSecond) {
        if (!Double.isFinite(netRatePerSecond)) {
            throw new IllegalArgumentException("A taxa térmica deve ser finita");
        }
        if (netRatePerSecond < -STABLE_RATE_PER_SECOND) {
            return ThermalTrend.COOLING;
        }
        if (netRatePerSecond > STABLE_RATE_PER_SECOND) {
            return ThermalTrend.WARMING;
        }
        return ThermalTrend.STABLE;
    }
}
