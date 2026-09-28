package dev.signalshards.livingworld.features.climate.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ArmorThermalCatalogTest {
    @Test
    void defaultsPreservamIdentidadeVanillaMaisSemDefinirMelhorArmaduraUniversal() {
        ArmorThermalCatalog catalog = ArmorThermalCatalog.livingWorldDefaults();

        assertTrue(
                catalog.profileFor(ArmorThermalMaterial.LEATHER).insulation()
                        > catalog.profileFor(ArmorThermalMaterial.CHAINMAIL).insulation()
        );
        assertTrue(
                catalog.profileFor(ArmorThermalMaterial.COPPER).insulation()
                        < catalog.profileFor(ArmorThermalMaterial.IRON).insulation()
        );
        assertTrue(
                catalog.profileFor(ArmorThermalMaterial.NETHERITE).insulation()
                        > catalog.profileFor(ArmorThermalMaterial.DIAMOND).insulation()
        );
        assertTrue(
                catalog.profileFor(ArmorThermalMaterial.TURTLE_SHELL).waterResistance()
                        > catalog.profileFor(ArmorThermalMaterial.IRON).waterResistance()
        );
    }
}
