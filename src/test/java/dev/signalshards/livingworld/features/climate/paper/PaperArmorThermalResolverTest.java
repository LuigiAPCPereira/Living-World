package dev.signalshards.livingworld.features.climate.paper;

import dev.signalshards.livingworld.features.climate.domain.ArmorThermalMaterial;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaperArmorThermalResolverTest {
    @Test
    void mapeiaMateriaisVanillaAtuais() {
        assertEquals(
                ArmorThermalMaterial.LEATHER,
                PaperArmorThermalResolver.materialFor(Material.LEATHER_CHESTPLATE).orElseThrow()
        );
        assertEquals(
                ArmorThermalMaterial.COPPER,
                PaperArmorThermalResolver.materialFor(Material.COPPER_HELMET).orElseThrow()
        );
        assertEquals(
                ArmorThermalMaterial.NETHERITE,
                PaperArmorThermalResolver.materialFor(Material.NETHERITE_BOOTS).orElseThrow()
        );
        assertEquals(
                ArmorThermalMaterial.TURTLE_SHELL,
                PaperArmorThermalResolver.materialFor(Material.TURTLE_HELMET).orElseThrow()
        );
    }

    @Test
    void itensSemPapelTermicoNaoEntramNoLoadout() {
        assertTrue(PaperArmorThermalResolver.materialFor(Material.ELYTRA).isEmpty());
        assertTrue(PaperArmorThermalResolver.materialFor(Material.AIR).isEmpty());
    }
}
