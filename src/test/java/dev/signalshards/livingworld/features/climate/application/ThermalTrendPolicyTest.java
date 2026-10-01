package dev.signalshards.livingworld.features.climate.application;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ThermalTrendPolicyTest {

    private final ThermalTrendPolicy policy = new ThermalTrendPolicy();

    @Test
    void classificaTaxaLiquidaEmEsfriandoEstavelEAquecendo() {
        assertEquals(ThermalTrend.COOLING, policy.classify(-0.001D));
        assertEquals(ThermalTrend.STABLE, policy.classify(-0.0005D));
        assertEquals(ThermalTrend.STABLE, policy.classify(0.0D));
        assertEquals(ThermalTrend.STABLE, policy.classify(0.0005D));
        assertEquals(ThermalTrend.WARMING, policy.classify(0.001D));
    }

    @Test
    void rejeitaTaxaNaoFinita() {
        assertThrows(
                IllegalArgumentException.class,
                () -> policy.classify(Double.NaN)
        );
    }
}
