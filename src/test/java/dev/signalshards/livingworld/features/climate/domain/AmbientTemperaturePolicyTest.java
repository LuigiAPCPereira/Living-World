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

    @Test
    void ceuAbertoAqueceAoMeioDiaEResfriaAMeiaNoite() {
        AmbientTemperature noon = policy.temperature(
                0.8D,
                Season.OUTONO,
                new AmbientTemperatureFactors(
                        1.0D,
                        6_000,
                        AmbientWeather.CLEAR
                )
        );
        AmbientTemperature midnight = policy.temperature(
                0.8D,
                Season.OUTONO,
                new AmbientTemperatureFactors(
                        1.0D,
                        18_000,
                        AmbientWeather.CLEAR
                )
        );

        assertEquals(17.5D, noon.degreesCelsius(), 1.0E-9D);
        assertEquals(11.5D, midnight.degreesCelsius(), 1.0E-9D);
    }

    @Test
    void weatherResfriaApenasNaParcelaExpostaAoCeu() {
        AmbientTemperature rain = policy.temperature(
                0.8D,
                Season.OUTONO,
                new AmbientTemperatureFactors(
                        1.0D,
                        6_000,
                        AmbientWeather.RAIN
                )
        );
        AmbientTemperature thunder = policy.temperature(
                0.8D,
                Season.OUTONO,
                new AmbientTemperatureFactors(
                        1.0D,
                        6_000,
                        AmbientWeather.THUNDER
                )
        );
        AmbientTemperature sheltered = policy.temperature(
                0.8D,
                Season.OUTONO,
                new AmbientTemperatureFactors(
                        0.0D,
                        18_000,
                        AmbientWeather.THUNDER
                )
        );

        assertEquals(16.0D, rain.degreesCelsius(), 1.0E-9D);
        assertEquals(15.0D, thunder.degreesCelsius(), 1.0E-9D);
        assertEquals(15.0D, sheltered.degreesCelsius(), 1.0E-9D);
    }
}
