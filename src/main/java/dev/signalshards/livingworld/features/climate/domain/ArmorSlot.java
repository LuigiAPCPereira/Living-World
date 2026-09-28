package dev.signalshards.livingworld.features.climate.domain;

/**
 * Cobertura corporal aproximada por slot de armadura.
 *
 * <p>Os pesos somam 1 e servem apenas à simulação térmica Vanilla+.</p>
 */
public enum ArmorSlot {
    HEAD(0.15D),
    CHEST(0.40D),
    LEGS(0.30D),
    FEET(0.15D);

    private final double coverageWeight;

    ArmorSlot(double coverageWeight) {
        this.coverageWeight = coverageWeight;
    }

    public double coverageWeight() {
        return coverageWeight;
    }
}
