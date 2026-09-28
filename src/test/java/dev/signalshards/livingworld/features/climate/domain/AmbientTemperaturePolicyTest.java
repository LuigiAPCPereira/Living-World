package dev.signalshards.livingworld.features.climate.domain;

import dev.signalshards.livingworld.features.seasons.domain.Season;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AmbientTemperaturePolicyTest {
    private final AmbientTemperaturePolicy policy = new AmbientTemperaturePolicy();

    @Test
    void preservaCalibracaoSazonalJaValidada() {
        assertEquals(17.0D, policy.temperature(0.8D, Season.PRIMAVERA).degreesCelsius());
        assertEquals(21.0D, policy.temperature(0.8D, Season.VERAO).degreesCelsius());
        assertEquals(15.0D, policy.temperature(0.8D, Season.OUTONO).degreesCelsius());
        assertEquals(9.0D, policy.temperature(0.8D, Season.INVERNO).degreesCelsius());
        assertEquals(19.0D, policy.temperature(1.0D, Season.OUTONO).degreesCelsius());
    }

    @Test
    void ambienteNaoHerdaClampDeApresentacaoDoHud() {
        assertEquals(205.0D, policy.temperature(10.0D, Season.VERAO).degreesCelsius());
        assertEquals(-207.0D, policy.temperature(-10.0D, Season.INVERNO).degreesCelsius());
    }

    @Test
    void rejeitaEntradaNaoFinita() {
        assertThrows(
                IllegalArgumentException.class,
                () -> policy.temperature(Double.NaN, Season.VERAO)
        );
    }
}
