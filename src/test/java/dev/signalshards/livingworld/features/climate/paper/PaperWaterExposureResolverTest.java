package dev.signalshards.livingworld.features.climate.paper;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaperWaterExposureResolverTest {
    @Test
    void quatroSondasDiferenciamContatoParcialESubmersao() {
        assertEquals(
                0.25D,
                PaperWaterExposureResolver.submergedFraction(true, false, false, false)
        );
        assertEquals(
                0.50D,
                PaperWaterExposureResolver.submergedFraction(true, true, false, false)
        );
        assertEquals(
                1.0D,
                PaperWaterExposureResolver.submergedFraction(true, true, true, true)
        );
    }

    @Test
    void profundidadeEncontraPrimeiroArAcimaSemScanLinear() {
        int depth = PaperWaterExposureResolver.depthBelowSurface(
                offset -> offset <= 7,
                32
        );

        assertEquals(7, depth);
    }

    @Test
    void profundidadeSaturaNoOrcamento() {
        assertEquals(
                32,
                PaperWaterExposureResolver.depthBelowSurface(offset -> true, 32)
        );
    }

    @Test
    void reconheceWaterloggedEBubbleColumnSemConfundirLava() {
        assertTrue(PaperWaterExposureResolver.isWaterMaterial(Material.WATER, false));
        assertTrue(PaperWaterExposureResolver.isWaterMaterial(Material.BUBBLE_COLUMN, false));
        assertTrue(PaperWaterExposureResolver.isWaterMaterial(Material.OAK_STAIRS, true));
        assertFalse(PaperWaterExposureResolver.isWaterMaterial(Material.LAVA, false));
    }

    @Test
    void entradasInvalidasFalham() {
        assertThrows(
                IllegalArgumentException.class,
                () -> PaperWaterExposureResolver.submergedFraction()
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> PaperWaterExposureResolver.depthBelowSurface(offset -> false, 0)
        );
    }
}
