package dev.signalshards.livingworld.features.climate.domain;

import dev.signalshards.livingworld.features.seasons.domain.Season;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApparentTemperaturePolicyTest {
    private final ApparentTemperaturePolicy policy = new ApparentTemperaturePolicy();

    @Test
    void pontoNeutroVariaComAEstacao() {
        assertEquals(17, policy.degreesCelsius(0.8, Season.PRIMAVERA));
        assertEquals(21, policy.degreesCelsius(0.8, Season.VERAO));
        assertEquals(15, policy.degreesCelsius(0.8, Season.OUTONO));
        assertEquals(9, policy.degreesCelsius(0.8, Season.INVERNO));
    }

    @Test
    void escalaContinuaEClampeada() {
        assertEquals(-40, policy.degreesCelsius(-10, Season.INVERNO));
        assertEquals(55, policy.degreesCelsius(10, Season.VERAO));
        assertEquals(19, policy.degreesCelsius(1.0, Season.OUTONO));
    }

    @Test
    void rejeitaTemperaturaNaoFinita() {
        assertThrows(
                IllegalArgumentException.class,
                () -> policy.degreesCelsius(Double.NaN, Season.VERAO)
        );
    }

    @Test
    void exposicaoDiretaAlteraApenasTemperaturaAparente() {
        assertEquals(
                11,
                policy.degreesCelsius(
                        0.8,
                        Season.OUTONO,
                        TemperatureExposure.WATER
                )
        );
        assertEquals(
                23,
                policy.degreesCelsius(
                        0.8,
                        Season.OUTONO,
                        TemperatureExposure.FIRE
                )
        );
        assertEquals(
                35,
                policy.degreesCelsius(
                        0.8,
                        Season.OUTONO,
                        TemperatureExposure.LAVA
                )
        );
    }

    @Test
    void exposicaoEmLavaRespeitaLimiteDoHud() {
        assertEquals(
                55,
                policy.degreesCelsius(
                        10.0,
                        Season.VERAO,
                        TemperatureExposure.LAVA
                )
        );
    }
}
