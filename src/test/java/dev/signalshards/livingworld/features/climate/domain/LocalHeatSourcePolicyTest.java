package dev.signalshards.livingworld.features.climate.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalHeatSourcePolicyTest {
    private final LocalHeatSourcePolicy policy = new LocalHeatSourcePolicy();

    @Test
    void fonteForaDoAlcanceNaoAquece() {
        ThermalExchangeRate rate = policy.exchangeRate(List.of(
                new LocalHeatExposure(LocalHeatSourceType.TORCH, 4.0D, 1.0D, 1.0D)
        ));
        assertEquals(0.0D, rate.loadPerSecond());
    }

    @Test
    void distanciaEObstrucaoReduzemContribuicao() {
        double near = policy.exchangeRate(List.of(
                new LocalHeatExposure(LocalHeatSourceType.CAMPFIRE, 0.0D, 1.0D, 1.0D)
        )).loadPerSecond();
        double far = policy.exchangeRate(List.of(
                new LocalHeatExposure(LocalHeatSourceType.CAMPFIRE, 4.0D, 1.0D, 1.0D)
        )).loadPerSecond();
        double obstructed = policy.exchangeRate(List.of(
                new LocalHeatExposure(LocalHeatSourceType.CAMPFIRE, 0.0D, 1.0D, 0.25D)
        )).loadPerSecond();

        assertTrue(near > far);
        assertTrue(near > obstructed);
    }

    @Test
    void ordemDeForcaVanillaMaisEhPreservada() {
        double torch = policy.exchangeRate(List.of(
                new LocalHeatExposure(LocalHeatSourceType.TORCH, 0.0D, 1.0D, 1.0D)
        )).loadPerSecond();
        double campfire = policy.exchangeRate(List.of(
                new LocalHeatExposure(LocalHeatSourceType.CAMPFIRE, 0.0D, 1.0D, 1.0D)
        )).loadPerSecond();
        double lava = policy.exchangeRate(List.of(
                new LocalHeatExposure(LocalHeatSourceType.LAVA, 0.0D, 1.0D, 1.0D)
        )).loadPerSecond();

        assertTrue(torch < campfire);
        assertTrue(campfire < lava);
    }

    @Test
    void muitasFontesNaoUltrapassamCapGlobal() {
        ThermalExchangeRate rate = policy.exchangeRate(List.of(
                new LocalHeatExposure(LocalHeatSourceType.LAVA, 0.0D, 1.0D, 1.0D),
                new LocalHeatExposure(LocalHeatSourceType.LAVA, 0.0D, 1.0D, 1.0D),
                new LocalHeatExposure(LocalHeatSourceType.LAVA, 0.0D, 1.0D, 1.0D)
        ));
        assertEquals(0.025D, rate.loadPerSecond(), 1.0E-9D);
    }
}
