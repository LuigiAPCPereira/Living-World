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

    @Test
    void temperaturaSazonalTransicionaSemDegrauEntreAnchors() {
        AmbientTemperatureFactors stable = new AmbientTemperatureFactors(
                0.0D,
                6_000,
                AmbientWeather.CLEAR
        );

        assertEquals(
                13.0D,
                policy.temperature(
                        0.8D,
                        Season.PRIMAVERA,
                        0.0D,
                        stable
                ).degreesCelsius(),
                1.0E-9D
        );
        assertEquals(
                17.0D,
                policy.temperature(
                        0.8D,
                        Season.PRIMAVERA,
                        0.5D,
                        stable
                ).degreesCelsius(),
                1.0E-9D
        );
        assertEquals(
                19.0D,
                policy.temperature(
                        0.8D,
                        Season.PRIMAVERA,
                        1.0D,
                        stable
                ).degreesCelsius(),
                1.0E-9D
        );
        assertEquals(
                policy.temperature(
                        0.8D,
                        Season.PRIMAVERA,
                        1.0D,
                        stable
                ).degreesCelsius(),
                policy.temperature(
                        0.8D,
                        Season.VERAO,
                        0.0D,
                        stable
                ).degreesCelsius(),
                1.0E-9D
        );
    }

    @Test
    void dimensaoNaoTerrestreIgnoraSeasonDiaEWeatherMasPreservaTemperaturaCoordenada() {
        EnvironmentalDimensionProfile nether =
                new EnvironmentalDimensionPolicy().profileFor(
                        EnvironmentalDimension.NETHER
                );
        AmbientTemperatureFactors exposedStorm = new AmbientTemperatureFactors(
                1.0D,
                18_000,
                AmbientWeather.THUNDER
        );

        double summer = policy.temperature(
                2.0D,
                Season.VERAO,
                0.5D,
                exposedStorm,
                nether
        ).degreesCelsius();
        double winter = policy.temperature(
                2.0D,
                Season.INVERNO,
                0.5D,
                exposedStorm,
                nether
        ).degreesCelsius();

        assertEquals(39.0D, summer, 1.0E-9D);
        assertEquals(summer, winter, 1.0E-9D);
    }
}
