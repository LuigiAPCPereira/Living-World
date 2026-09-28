package dev.signalshards.livingworld.features.climate.paper;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaperWindShelterResolverTest {
    @Test
    void tetoCompletoRemoveVentoEProduzAbrigoMaximo() {
        var observation = PaperWindShelterResolver.resolve(
                0.0D,
                1.0D,
                1.0D,
                1.0D
        );

        assertEquals(0.0D, observation.windExposure().level());
        assertEquals(1.0D, observation.shelterFactor().level());
    }

    @Test
    void tempestadeEAltitudeAumentamVentoQuandoCeuEstaAberto() {
        var clearLow = PaperWindShelterResolver.resolve(
                1.0D,
                0.0D,
                0.0D,
                0.0D
        );
        var stormPeak = PaperWindShelterResolver.resolve(
                1.0D,
                1.0D,
                1.0D,
                0.0D
        );

        assertTrue(
                stormPeak.windExposure().level()
                        > clearLow.windExposure().level()
        );
        assertEquals(0.0D, stormPeak.shelterFactor().level());
    }

    @Test
    void movimentoRapidoPodeDominarVentoAmbiental() {
        var slow = PaperWindShelterResolver.resolve(
                1.0D,
                0.0D,
                0.0D,
                0.0D
        );
        var fast = PaperWindShelterResolver.resolve(
                1.0D,
                0.0D,
                0.0D,
                0.90D
        );

        assertTrue(fast.windExposure().level() > slow.windExposure().level());
    }

    @Test
    void valoresForaDaFaixaFalham() {
        assertThrows(
                IllegalArgumentException.class,
                () -> PaperWindShelterResolver.resolve(
                        1.1D, 0.0D, 0.0D, 0.0D
                )
        );
    }
}
