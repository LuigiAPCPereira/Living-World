package dev.signalshards.livingworld.features.ecology.domain;

import dev.signalshards.livingworld.features.climate.domain.ClimateSnapshot;
import dev.signalshards.livingworld.features.climate.domain.MoistureBand;
import dev.signalshards.livingworld.features.climate.domain.ThermalBand;

import java.util.Objects;

/**
 * Chance de preservar uma tentativa vanilla de perda de umidade do FARMLAND.
 *
 * <p>A política nunca hidrata solo nem acelera secagem. Ela apenas pode
 * cancelar parte das quedas de moisture em clima frio/úmido.</p>
 */
public final class FarmlandMoistureRetentionPolicy {
    private static final double MAX_BASE_RETENTION = 0.75D;

    public double retentionChance(ClimateSnapshot climate, double strength) {
        Objects.requireNonNull(climate, "clima");
        if (!Double.isFinite(strength) || strength < 0.0D || strength > 1.0D) {
            throw new IllegalArgumentException(
                    "A força de retenção deve ficar entre 0 e 1"
            );
        }

        double base = Math.min(
                MAX_BASE_RETENTION,
                thermalRetention(climate.temperature())
                        + moistureRetention(climate.moisture())
        );
        return base * strength;
    }

    private double thermalRetention(ThermalBand temperature) {
        return switch (temperature) {
            case CONGELANTE -> 0.45D;
            case FRIO -> 0.25D;
            case TEMPERADO -> 0.10D;
            case QUENTE, ESCALDANTE -> 0.0D;
        };
    }

    private double moistureRetention(MoistureBand moisture) {
        return switch (moisture) {
            case ARIDO, SECO -> 0.0D;
            case EQUILIBRADO -> 0.10D;
            case UMIDO -> 0.30D;
            case ENCHARCADO -> 0.50D;
        };
    }
}
