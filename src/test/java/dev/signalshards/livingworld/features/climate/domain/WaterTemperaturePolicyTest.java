package dev.signalshards.livingworld.features.climate.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WaterTemperaturePolicyTest {
    private final WaterTemperaturePolicy policy = new WaterTemperaturePolicy();

    @Test
    void superficieAcompanhaParcialmenteTemperaturaAmbiente() {
        WaterTemperature mild = policy.temperature(new AmbientTemperature(15.0D), 0.0D);
        WaterTemperature hot = policy.temperature(new AmbientTemperature(35.0D), 0.0D);

        assertEquals(12.55D, mild.degreesCelsius(), 1.0E-9D);
        assertEquals(25.55D, hot.degreesCelsius(), 1.0E-9D);
        assertTrue(hot.degreesCelsius() < 35.0D);
    }

    @Test
    void aguaLiquidaSuperficialNaoCaiAbaixoDoMinimoConfigurado() {
        WaterTemperature freezing = policy.temperature(
                new AmbientTemperature(-20.0D),
                0.0D
        );

        assertEquals(0.0D, freezing.degreesCelsius());
    }

    @Test
    void profundidadeEstabilizaTemperatura() {
        WaterTemperature hotSurface = policy.temperature(new AmbientTemperature(35.0D), 0.0D);
        WaterTemperature hotDeep = policy.temperature(new AmbientTemperature(35.0D), 32.0D);
        WaterTemperature coldSurface = policy.temperature(new AmbientTemperature(-20.0D), 0.0D);
        WaterTemperature coldDeep = policy.temperature(new AmbientTemperature(-20.0D), 32.0D);

        assertTrue(hotDeep.degreesCelsius() < hotSurface.degreesCelsius());
        assertTrue(hotDeep.degreesCelsius() > 8.0D);
        assertTrue(coldDeep.degreesCelsius() > coldSurface.degreesCelsius());
        assertTrue(coldDeep.degreesCelsius() < 8.0D);
    }

    @Test
    void estabilizacaoPorProfundidadeESaturada() {
        WaterTemperature reference = policy.temperature(new AmbientTemperature(30.0D), 32.0D);
        WaterTemperature abyssal = policy.temperature(new AmbientTemperature(30.0D), 512.0D);

        assertEquals(reference.degreesCelsius(), abyssal.degreesCelsius(), 1.0E-9D);
    }
}
