package dev.signalshards.livingworld.features.climate.paper;

import dev.signalshards.livingworld.features.climate.domain.DirectThermalExposure;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaperDirectThermalExposureResolverTest {
    @Test
    void lavaTemPrioridadeSobreOutrasExposicoes() {
        assertEquals(
                DirectThermalExposure.LAVA,
                PaperDirectThermalExposureResolver.resolve(
                        true,
                        true,
                        true,
                        100
                )
        );
    }

    @Test
    void powderSnowTemPrioridadeSobreFogoResidual() {
        assertEquals(
                DirectThermalExposure.POWDER_SNOW,
                PaperDirectThermalExposureResolver.resolve(
                        false,
                        true,
                        false,
                        100
                )
        );
    }

    @Test
    void aguaSuprimeFogoResidualComoExposicaoDireta() {
        assertEquals(
                DirectThermalExposure.NONE,
                PaperDirectThermalExposureResolver.resolve(
                        false,
                        false,
                        true,
                        100
                )
        );
    }

    @Test
    void fogoSoEntraQuandoNaoHaMeioFisicoComPrioridade() {
        assertEquals(
                DirectThermalExposure.FIRE,
                PaperDirectThermalExposureResolver.resolve(
                        false,
                        false,
                        false,
                        20
                )
        );
        assertEquals(
                DirectThermalExposure.NONE,
                PaperDirectThermalExposureResolver.resolve(
                        false,
                        false,
                        false,
                        0
                )
        );
    }
}
