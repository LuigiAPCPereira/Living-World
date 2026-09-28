package dev.signalshards.livingworld.features.climate.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ArmorThermalLoadoutTest {
    @Test
    void rejeitaDuasPecasNoMesmoSlot() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ArmorThermalLoadout(List.of(
                        new EquippedArmorPiece(ArmorSlot.HEAD, ArmorThermalMaterial.LEATHER),
                        new EquippedArmorPiece(ArmorSlot.HEAD, ArmorThermalMaterial.IRON)
                ))
        );
    }
}
