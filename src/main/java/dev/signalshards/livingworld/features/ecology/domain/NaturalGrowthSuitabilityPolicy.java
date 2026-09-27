package dev.signalshards.livingworld.features.ecology.domain;

import dev.signalshards.livingworld.features.climate.domain.ClimateSnapshot;
import dev.signalshards.livingworld.features.climate.domain.MoistureBand;
import dev.signalshards.livingworld.features.climate.domain.ThermalBand;

import java.util.Objects;

/**
 * Calcula a chance de uma tentativa de crescimento natural do vanilla ser
 * aceita pelo Living World.
 *
 * <p>A política nunca retorna mais de 1.0; portanto, não acelera crescimento
 * acima do vanilla. {@code strength=0} equivale a vanilla e {@code 1} aplica
 * toda a penalidade climática.</p>
 */
public final class NaturalGrowthSuitabilityPolicy {
    public double acceptanceChance(ClimateSnapshot climate, double strength) {
        Objects.requireNonNull(climate, "clima");
        if (!Double.isFinite(strength) || strength < 0.0D || strength > 1.0D) {
            throw new IllegalArgumentException(
                    "A força ecológica deve ficar entre 0 e 1"
            );
        }

        double baseSuitability = thermalFactor(climate.temperature())
                * moistureFactor(climate.moisture());
        return 1.0D - (strength * (1.0D - baseSuitability));
    }

    private double thermalFactor(ThermalBand temperature) {
        return switch (temperature) {
            case CONGELANTE -> 0.35D;
            case FRIO -> 0.70D;
            case TEMPERADO -> 1.00D;
            case QUENTE -> 0.85D;
            case ESCALDANTE -> 0.45D;
        };
    }

    private double moistureFactor(MoistureBand moisture) {
        return switch (moisture) {
            case ARIDO -> 0.50D;
            case SECO -> 0.80D;
            case EQUILIBRADO, UMIDO -> 1.00D;
            case ENCHARCADO -> 0.85D;
        };
    }
}
