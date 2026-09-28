package dev.signalshards.livingworld.features.climate.application;

import dev.signalshards.livingworld.features.climate.domain.AmbientTemperature;
import dev.signalshards.livingworld.features.climate.domain.ArmorSlot;
import dev.signalshards.livingworld.features.climate.domain.ArmorThermalLoadout;
import dev.signalshards.livingworld.features.climate.domain.ArmorThermalMaterial;
import dev.signalshards.livingworld.features.climate.domain.EquippedArmorPiece;
import dev.signalshards.livingworld.features.climate.domain.LocalHeatExposure;
import dev.signalshards.livingworld.features.climate.domain.LocalHeatSourceType;
import dev.signalshards.livingworld.features.climate.domain.PlayerActivity;
import dev.signalshards.livingworld.features.climate.domain.PlayerThermalState;
import dev.signalshards.livingworld.features.climate.domain.ShelterFactor;
import dev.signalshards.livingworld.features.climate.domain.WaterExposure;
import dev.signalshards.livingworld.features.climate.domain.WetnessState;
import dev.signalshards.livingworld.features.climate.domain.WindExposure;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThermalExchangeComposerTest {
    private final ThermalExchangeComposer composer = new ThermalExchangeComposer();

    @Test
    void contextoNeutroProduzTaxasNeutras() {
        ThermalExchangeResolution result = composer.resolve(context(
                new AmbientTemperature(20.0D),
                WaterExposure.none(),
                PlayerActivity.RESTING,
                WetnessState.dry(),
                ArmorThermalLoadout.empty(),
                List.of()
        ));

        assertEquals(0.0D, result.netRate().loadPerSecond(), 1.0E-9D);
        assertEquals(0.0D, result.wetnessRate().levelPerSecond(), 1.0E-9D);
        assertFalse(result.waterTemperature().isPresent());
    }

    @Test
    void submersaoTotalRemoveTrocaComArEUsaAgua() {
        ThermalExchangeResolution result = composer.resolve(context(
                new AmbientTemperature(-10.0D),
                new WaterExposure(1.0D, 0.0D),
                PlayerActivity.RESTING,
                WetnessState.dry(),
                ArmorThermalLoadout.empty(),
                List.of()
        ));

        assertEquals(0.0D, result.airRate().loadPerSecond(), 1.0E-9D);
        assertTrue(result.waterRate().loadPerSecond() < 0.0D);
        assertTrue(result.wetnessRate().levelPerSecond() > 0.0D);
        assertEquals(0.0D, result.waterTemperature().orElseThrow().degreesCelsius());
    }

    @Test
    void armaduraAfetaArEWetnessNoMesmoContexto() {
        ArmorThermalLoadout leather = fullLeather();
        ThermalExchangeContext noArmor = context(
                new AmbientTemperature(-5.0D),
                new WaterExposure(0.5D, 0.0D),
                PlayerActivity.RESTING,
                WetnessState.dry(),
                ArmorThermalLoadout.empty(),
                List.of()
        );
        ThermalExchangeContext armored = new ThermalExchangeContext(
                noArmor.thermalState(),
                noArmor.wetness(),
                noArmor.ambientTemperature(),
                noArmor.waterExposure(),
                noArmor.activity(),
                noArmor.windExposure(),
                noArmor.shelterFactor(),
                leather,
                noArmor.localHeatExposures()
        );

        ThermalExchangeResolution plain = composer.resolve(noArmor);
        ThermalExchangeResolution protectedResult = composer.resolve(armored);

        assertTrue(
                Math.abs(protectedResult.airRate().loadPerSecond())
                        < Math.abs(plain.airRate().loadPerSecond())
        );
        assertTrue(
                protectedResult.wetnessRate().levelPerSecond()
                        < plain.wetnessRate().levelPerSecond()
        );
    }

    @Test
    void atividadeECalorLocalEntramNoBreakdownSeparadamente() {
        ThermalExchangeResolution result = composer.resolve(context(
                new AmbientTemperature(20.0D),
                WaterExposure.none(),
                PlayerActivity.SPRINTING,
                WetnessState.dry(),
                ArmorThermalLoadout.empty(),
                List.of(new LocalHeatExposure(
                        LocalHeatSourceType.TORCH,
                        0.0D,
                        1.0D,
                        1.0D
                ))
        ));

        assertTrue(result.activityRate().loadPerSecond() > 0.0D);
        assertTrue(result.localHeatRate().loadPerSecond() > 0.0D);
        assertEquals(
                result.activityRate().loadPerSecond() + result.localHeatRate().loadPerSecond(),
                result.netRate().loadPerSecond(),
                1.0E-9D
        );
    }

    private ThermalExchangeContext context(
            AmbientTemperature ambient,
            WaterExposure water,
            PlayerActivity activity,
            WetnessState wetness,
            ArmorThermalLoadout armor,
            List<LocalHeatExposure> heat
    ) {
        return new ThermalExchangeContext(
                PlayerThermalState.neutral(),
                wetness,
                ambient,
                water,
                activity,
                WindExposure.calm(),
                ShelterFactor.exposed(),
                armor,
                heat
        );
    }

    private ArmorThermalLoadout fullLeather() {
        return new ArmorThermalLoadout(List.of(
                new EquippedArmorPiece(ArmorSlot.HEAD, ArmorThermalMaterial.LEATHER),
                new EquippedArmorPiece(ArmorSlot.CHEST, ArmorThermalMaterial.LEATHER),
                new EquippedArmorPiece(ArmorSlot.LEGS, ArmorThermalMaterial.LEATHER),
                new EquippedArmorPiece(ArmorSlot.FEET, ArmorThermalMaterial.LEATHER)
        ));
    }
}
