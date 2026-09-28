package dev.signalshards.livingworld.features.climate.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class LocalHeatExposureTest {
    @Test
    void rejeitaObservacaoInvalida() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new LocalHeatExposure(LocalHeatSourceType.TORCH, -1.0D, 1.0D, 1.0D)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new LocalHeatExposure(LocalHeatSourceType.TORCH, 1.0D, 1.1D, 1.0D)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new LocalHeatExposure(LocalHeatSourceType.TORCH, 1.0D, 1.0D, -0.1D)
        );
    }
}
