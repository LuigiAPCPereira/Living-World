package dev.signalshards.livingworld.features.climate.domain;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ArmorWetnessScenarioTest {
    @Test
    void conjuntoDeCouroAtrasaSaturacaoNaAguaSemImpermeabilizar() {
        WaterExposureEffect water = new WaterExposurePolicy().evaluate(
                new WaterExposure(1.0D, 0.0D)
        );
        ArmorWetnessPolicy armorPolicy = new ArmorWetnessPolicy();
        WetnessPolicy wetnessPolicy = new WetnessPolicy();
        ArmorThermalLoadout leather = new ArmorThermalLoadout(List.of(
                new EquippedArmorPiece(ArmorSlot.HEAD, ArmorThermalMaterial.LEATHER),
                new EquippedArmorPiece(ArmorSlot.CHEST, ArmorThermalMaterial.LEATHER),
                new EquippedArmorPiece(ArmorSlot.LEGS, ArmorThermalMaterial.LEATHER),
                new EquippedArmorPiece(ArmorSlot.FEET, ArmorThermalMaterial.LEATHER)
        ));

        WetnessState unarmored = WetnessState.dry();
        WetnessState protectedState = WetnessState.dry();
        WetnessRate protectedRate = armorPolicy.apply(water.wetnessRate(), leather);
        for (int second = 0; second < 5; second++) {
            unarmored = wetnessPolicy.advance(
                    unarmored,
                    water.wetnessRate(),
                    Duration.ofSeconds(1)
            );
            protectedState = wetnessPolicy.advance(
                    protectedState,
                    protectedRate,
                    Duration.ofSeconds(1)
            );
        }

        assertTrue(protectedState.level() < unarmored.level());
        assertTrue(protectedState.level() > 0.0D);
    }
}
