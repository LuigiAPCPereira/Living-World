package dev.signalshards.livingworld.features.ecology.domain;

import dev.signalshards.livingworld.features.climate.domain.AmbientTemperature;

import java.util.Objects;

public final class WinterThermalPhasePolicy {
    private final PhysicalWinterSettings settings;

    public WinterThermalPhasePolicy(PhysicalWinterSettings settings) {
        this.settings = Objects.requireNonNull(
                settings,
                "configuração de inverno físico"
        );
    }

    public WinterThermalPhase phaseFor(AmbientTemperature ambient) {
        Objects.requireNonNull(ambient, "temperatura ambiente");
        double celsius = ambient.degreesCelsius();
        if (celsius <= settings.freezeAtOrBelowCelsius()) {
            return WinterThermalPhase.FREEZING;
        }
        if (celsius >= settings.thawAtOrAboveCelsius()) {
            return WinterThermalPhase.THAWING;
        }
        return WinterThermalPhase.HOLDING;
    }
}
