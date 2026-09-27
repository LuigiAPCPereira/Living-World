package dev.signalshards.livingworld.features.ecology.domain;

import dev.signalshards.livingworld.features.climate.domain.ClimateSnapshot;
import dev.signalshards.livingworld.features.climate.domain.MoistureBand;
import dev.signalshards.livingworld.features.climate.domain.ThermalBand;
import dev.signalshards.livingworld.features.climate.domain.WeatherTendency;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FrozenSurfacePolicyTest {
    private final FrozenSurfacePolicy policy = new FrozenSurfacePolicy();

    @Test
    void frioECompatívelComFormacaoEPreservacao() {
        assertTrue(policy.allowFormation(snapshot(ThermalBand.CONGELANTE)));
        assertTrue(policy.allowFormation(snapshot(ThermalBand.FRIO)));
        assertTrue(policy.preserveFromFade(snapshot(ThermalBand.FRIO)));
    }

    @Test
    void temperadoOuMaisQuenteNaoPreservaSuperficieCongelada() {
        assertFalse(policy.allowFormation(snapshot(ThermalBand.TEMPERADO)));
        assertFalse(policy.preserveFromFade(snapshot(ThermalBand.QUENTE)));
        assertFalse(policy.preserveFromFade(snapshot(ThermalBand.ESCALDANTE)));
    }

    private ClimateSnapshot snapshot(ThermalBand thermal) {
        return new ClimateSnapshot(
                thermal,
                MoistureBand.EQUILIBRADO,
                WeatherTendency.ESTAVEL
        );
    }
}
