package dev.signalshards.livingworld.features.climate.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArmorInsulationPolicyTest {
    private final ArmorInsulationPolicy policy = new ArmorInsulationPolicy();

    @Test
    void semArmaduraMantemFatorBase() {
        AirThermalTransferFactor result = policy.apply(
                AirThermalTransferFactor.baseline(),
                ArmorThermalLoadout.empty()
        );

        assertEquals(1.0D, result.value(), 1.0E-9D);
    }

    @Test
    void conjuntoCompletoIsolaMaisQueApenasBotas() {
        ArmorThermalLoadout boots = new ArmorThermalLoadout(List.of(
                new EquippedArmorPiece(ArmorSlot.FEET, ArmorThermalMaterial.LEATHER)
        ));
        ArmorThermalLoadout full = fullSet(ArmorThermalMaterial.LEATHER);

        assertTrue(policy.effectiveInsulation(full) > policy.effectiveInsulation(boots));
        assertTrue(
                policy.apply(AirThermalTransferFactor.baseline(), full).value()
                        < policy.apply(AirThermalTransferFactor.baseline(), boots).value()
        );
    }

    @Test
    void couroIsolaMaisQueChainmailNoMesmoConjunto() {
        double leather = policy.effectiveInsulation(fullSet(ArmorThermalMaterial.LEATHER));
        double chainmail = policy.effectiveInsulation(fullSet(ArmorThermalMaterial.CHAINMAIL));

        assertTrue(leather > chainmail);
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
