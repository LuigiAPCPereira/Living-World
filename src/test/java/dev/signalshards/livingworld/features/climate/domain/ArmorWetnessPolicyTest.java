package dev.signalshards.livingworld.features.climate.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArmorWetnessPolicyTest {
    private final ArmorWetnessPolicy policy = new ArmorWetnessPolicy();

    @Test
    void semArmaduraNaoAlteraMolhamento() {
        WetnessRate base = new WetnessRate(0.12D);
        assertEquals(
                base,
                policy.apply(base, ArmorThermalLoadout.empty())
        );
    }

    @Test
    void couroRetardaMasNaoBloqueiaMolhamento() {
        WetnessRate base = new WetnessRate(0.12D);
        WetnessRate protectedRate = policy.apply(
                base,
                fullSet(ArmorThermalMaterial.LEATHER)
        );

        assertTrue(protectedRate.levelPerSecond() > 0.0D);
        assertTrue(protectedRate.levelPerSecond() < base.levelPerSecond());
    }

    @Test
    void turtleShellProtegeMaisQueCapaceteDeFerroContraEntradaDeAgua() {
        ArmorThermalLoadout turtle = new ArmorThermalLoadout(List.of(
                new EquippedArmorPiece(ArmorSlot.HEAD, ArmorThermalMaterial.TURTLE_SHELL)
        ));
        ArmorThermalLoadout iron = new ArmorThermalLoadout(List.of(
                new EquippedArmorPiece(ArmorSlot.HEAD, ArmorThermalMaterial.IRON)
        ));

        assertTrue(
                policy.effectiveWaterResistance(turtle)
                        > policy.effectiveWaterResistance(iron)
        );
    }

    @Test
    void secagemNaoEhAlteradaNesteSlice() {
        WetnessRate drying = new WetnessRate(-0.02D);
        assertEquals(
                drying,
                policy.apply(drying, fullSet(ArmorThermalMaterial.LEATHER))
        );
    }

    private ArmorThermalLoadout fullSet(ArmorThermalMaterial material) {
        return new ArmorThermalLoadout(List.of(
                new EquippedArmorPiece(ArmorSlot.HEAD, material),
                new EquippedArmorPiece(ArmorSlot.CHEST, material),
                new EquippedArmorPiece(ArmorSlot.LEGS, material),
                new EquippedArmorPiece(ArmorSlot.FEET, material)
        ));
    }
}
