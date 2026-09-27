package dev.signalshards.livingworld.features.ecology.domain;

import dev.signalshards.livingworld.features.climate.domain.ClimateSnapshot;
import dev.signalshards.livingworld.features.climate.domain.ThermalBand;

import java.util.Objects;

public final class FrozenSurfacePolicy {
    public boolean allowFormation(ClimateSnapshot climate) {
        Objects.requireNonNull(climate, "clima");
        return isColdEnough(climate.temperature());
    }

    public boolean preserveFromFade(ClimateSnapshot climate) {
        Objects.requireNonNull(climate, "clima");
        return isColdEnough(climate.temperature());
    }

    private boolean isColdEnough(ThermalBand temperature) {
        return temperature == ThermalBand.CONGELANTE
                || temperature == ThermalBand.FRIO;
    }
}
