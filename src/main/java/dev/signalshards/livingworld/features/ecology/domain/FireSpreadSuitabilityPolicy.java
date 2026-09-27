package dev.signalshards.livingworld.features.ecology.domain;

import dev.signalshards.livingworld.features.climate.domain.ClimateSnapshot;
import dev.signalshards.livingworld.features.climate.domain.MoistureBand;
import dev.signalshards.livingworld.features.climate.domain.ThermalBand;

import java.util.Objects;

/**
 * Chance de uma tentativa vanilla de propagação de fogo ser aceita.
 *
 * <p>A política nunca cria novas tentativas nem aumenta a frequência do
 * vanilla. Hot/dry pode permanecer em 1.0; cold/wet reduz a chance.</p>
 */
public final class FireSpreadSuitabilityPolicy {
    public double acceptanceChance(ClimateSnapshot climate, double strength) {
        Objects.requireNonNull(climate, "clima");
        if (!Double.isFinite(strength) || strength < 0.0D || strength > 1.0D) {
            throw new IllegalArgumentException(
                    "A força de propagação do fogo deve ficar entre 0 e 1"
            );
        }

        double base = thermalFactor(climate.temperature())
                * moistureFactor(climate.moisture());
        return 1.0D - (strength * (1.0D - base));
    }

    private double thermalFactor(ThermalBand thermal) {
        return switch (thermal) {
            case CONGELANTE -> 0.45D;
            case FRIO -> 0.65D;
            case TEMPERADO -> 0.85D;
            case QUENTE, ESCALDANTE -> 1.0D;
        };
    }

    private double moistureFactor(MoistureBand moisture) {
        return switch (moisture) {
            case ARIDO, SECO -> 1.0D;
            case EQUILIBRADO -> 0.85D;
            case UMIDO -> 0.55D;
            case ENCHARCADO -> 0.30D;
        };
    }
}
