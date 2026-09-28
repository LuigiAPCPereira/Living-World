package dev.signalshards.livingworld.features.climate.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ArmorThermalScenarioTest {
    private final ArmorInsulationPolicy insulationPolicy = new ArmorInsulationPolicy();
    private final AirThermalExchangePolicy airPolicy = new AirThermalExchangePolicy();

    @Test
    void couroReduzPerdaDeCalorNoFrio() {
        AmbientTemperature cold = new AmbientTemperature(-10.0D);
        PlayerThermalState neutral = PlayerThermalState.neutral();

        ThermalExchangeRate unarmored = airPolicy.exchangeRate(
                neutral,
                cold,
                WetnessState.dry(),
                AirThermalTransferFactor.baseline()
        );
        ThermalExchangeRate leather = airPolicy.exchangeRate(
                neutral,
                cold,
                WetnessState.dry(),
                insulationPolicy.apply(
                        AirThermalTransferFactor.baseline(),
                        fullSet(ArmorThermalMaterial.LEATHER)
                )
        );

        assertTrue(Math.abs(leather.loadPerSecond()) < Math.abs(unarmored.loadPerSecond()));
    }

    @Test
    void isolamentoTambemRetemCalorQuandoCorpoPrecisaEsfriar() {
        AmbientTemperature mild = new AmbientTemperature(20.0D);
        PlayerThermalState hotBody = new PlayerThermalState(0.80D);

        ThermalExchangeRate unarmored = airPolicy.exchangeRate(
                hotBody,
                mild,
                WetnessState.dry(),
                AirThermalTransferFactor.baseline()
        );
        ThermalExchangeRate leather = airPolicy.exchangeRate(
                hotBody,
                mild,
                WetnessState.dry(),
                insulationPolicy.apply(
                        AirThermalTransferFactor.baseline(),
                        fullSet(ArmorThermalMaterial.LEATHER)
                )
        );

        assertTrue(unarmored.loadPerSecond() < 0.0D);
        assertTrue(leather.loadPerSecond() < 0.0D);
        assertTrue(Math.abs(leather.loadPerSecond()) < Math.abs(unarmored.loadPerSecond()));
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
