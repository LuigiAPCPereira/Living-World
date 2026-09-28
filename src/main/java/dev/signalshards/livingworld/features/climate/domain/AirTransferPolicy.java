package dev.signalshards.livingworld.features.climate.domain;

import java.util.Objects;

/**
 * Combina vento e abrigo em um fator bounded de transferência com o ar.
 *
 * <p>O cálculo é geométrico/ambiental em intenção, não semântico: adapters
 * futuros podem derivar vento/abrigo por probes bounded sem identificar
 * construções como casas.</p>
 */
public final class AirTransferPolicy {
    private final AirTransferSettings settings;

    public AirTransferPolicy() {
        this(AirTransferSettings.livingWorldDefaults());
    }

    public AirTransferPolicy(AirTransferSettings settings) {
        this.settings = Objects.requireNonNull(settings, "configuração de transferência do ar");
    }

    public AirThermalTransferFactor factorFor(
            WindExposure windExposure,
            ShelterFactor shelterFactor
    ) {
        Objects.requireNonNull(windExposure, "exposição ao vento");
        Objects.requireNonNull(shelterFactor, "abrigo");

        double windMultiplier = 1.0D + (
                windExposure.level() * settings.maxWindBonus()
        );
        double shelterMultiplier = 1.0D - (
                shelterFactor.level() * settings.maxShelterReduction()
        );
        double factor = windMultiplier * shelterMultiplier;

        return new AirThermalTransferFactor(Math.clamp(
                factor,
                settings.minimumTransferFactor(),
                settings.maximumTransferFactor()
        ));
    }
}
