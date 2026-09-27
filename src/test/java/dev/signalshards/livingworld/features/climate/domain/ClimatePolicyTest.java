package dev.signalshards.livingworld.features.climate.domain;

import dev.signalshards.livingworld.features.seasons.domain.Season;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClimatePolicyTest {
    private final ClimatePolicy policy = new ClimatePolicy();

    @Test
    void veraoAqueceESecaModeradamenteUmBiomaTemperado() {
        ClimateProfile profile = new ClimateProfile(ThermalBand.TEMPERADO, MoistureBand.EQUILIBRADO);

        ClimateSnapshot snapshot = policy.evaluate(profile, Season.VERAO, ClimateState.stable());

        assertEquals(ThermalBand.QUENTE, snapshot.temperature());
        assertEquals(MoistureBand.SECO, snapshot.moisture());
        assertEquals(WeatherTendency.TEMPO_LIMPO, snapshot.weatherTendency());
    }

    @Test
    void invernoEsfriaSemUltrapassarOLimiteTermico() {
        ClimateProfile profile = new ClimateProfile(ThermalBand.CONGELANTE, MoistureBand.EQUILIBRADO);

        ClimateSnapshot snapshot = policy.evaluate(profile, Season.INVERNO, new ClimateState(-2, 0, 0));

        assertEquals(ThermalBand.CONGELANTE, snapshot.temperature());
    }

    @Test
    void primaveraFavorecePrecipitacaoEmRegiaoJaUmida() {
        ClimateProfile profile = new ClimateProfile(ThermalBand.TEMPERADO, MoistureBand.UMIDO);

        ClimateSnapshot snapshot = policy.evaluate(profile, Season.PRIMAVERA, ClimateState.stable());

        assertEquals(MoistureBand.ENCHARCADO, snapshot.moisture());
        assertEquals(WeatherTendency.PRECIPITACAO, snapshot.weatherTendency());
    }

    @Test
    void pressaoMaximaSoViraTempestadeComUmidadeSuficiente() {
        ClimateState stormState = ClimateState.stable().adjustStormPressure(2);

        ClimateSnapshot wet = policy.evaluate(
                new ClimateProfile(ThermalBand.QUENTE, MoistureBand.ENCHARCADO),
                Season.OUTONO,
                stormState
        );
        ClimateSnapshot dry = policy.evaluate(
                new ClimateProfile(ThermalBand.QUENTE, MoistureBand.SECO),
                Season.OUTONO,
                stormState
        );

        assertEquals(WeatherTendency.TEMPESTADE, wet.weatherTendency());
        assertEquals(WeatherTendency.TEMPO_LIMPO, dry.weatherTendency());
    }

    @Test
    void mesmoEstadoProduzResultadoDiferenteConformePerfilDoBioma() {
        ClimateState state = ClimateState.stable();

        ClimateSnapshot arid = policy.evaluate(
                new ClimateProfile(ThermalBand.QUENTE, MoistureBand.ARIDO),
                Season.OUTONO,
                state
        );
        ClimateSnapshot humid = policy.evaluate(
                new ClimateProfile(ThermalBand.QUENTE, MoistureBand.UMIDO),
                Season.OUTONO,
                state
        );

        assertEquals(WeatherTendency.TEMPO_LIMPO, arid.weatherTendency());
        assertEquals(WeatherTendency.PRECIPITACAO, humid.weatherTendency());
    }
}
