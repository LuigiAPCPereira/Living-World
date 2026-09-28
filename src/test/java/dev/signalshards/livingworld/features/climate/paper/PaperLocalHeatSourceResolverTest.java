package dev.signalshards.livingworld.features.climate.paper;

import dev.signalshards.livingworld.features.climate.domain.LocalHeatSourceType;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaperLocalHeatSourceResolverTest {
    @Test
    void mapeiaFontesVanillaSemTratarFornalhaApagadaComoCalor() {
        assertEquals(
                LocalHeatSourceType.TORCH,
                PaperLocalHeatSourceResolver.sourceType(Material.TORCH, true).orElseThrow()
        );
        assertEquals(
                LocalHeatSourceType.CAMPFIRE,
                PaperLocalHeatSourceResolver.sourceType(Material.CAMPFIRE, true).orElseThrow()
        );
        assertEquals(
                LocalHeatSourceType.LAVA,
                PaperLocalHeatSourceResolver.sourceType(Material.LAVA, true).orElseThrow()
        );
        assertTrue(
                PaperLocalHeatSourceResolver.sourceType(Material.FURNACE, false).isEmpty()
        );
        assertTrue(
                PaperLocalHeatSourceResolver.sourceType(Material.CAMPFIRE, false).isEmpty()
        );
    }

    @Test
    void materiaisIrrelevantesNaoViramFontesTermicas() {
        assertTrue(
                PaperLocalHeatSourceResolver.sourceType(Material.GLOWSTONE, true).isEmpty()
        );
        assertTrue(
                PaperLocalHeatSourceResolver.sourceType(Material.REDSTONE_LAMP, true).isEmpty()
        );
    }
}
