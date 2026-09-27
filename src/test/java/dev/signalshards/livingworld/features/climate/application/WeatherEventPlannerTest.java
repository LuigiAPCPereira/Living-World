package dev.signalshards.livingworld.features.climate.application;

import dev.signalshards.livingworld.features.climate.domain.ClimateSnapshot;
import dev.signalshards.livingworld.features.climate.domain.MoistureBand;
import dev.signalshards.livingworld.features.climate.domain.ThermalBand;
import dev.signalshards.livingworld.features.climate.domain.WeatherTendency;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WeatherEventPlannerTest {
    private final WeatherEventSettings settings = WeatherEventSettings.defaults();
    private final WeatherEventPlanner planner = new WeatherEventPlanner(settings);

    @Test
    void naoForcaEventoQuandoClimaEstaEstavel() {
        assertTrue(planner.plan(snapshot(WeatherTendency.ESTAVEL)).isEmpty());
    }

    @Test
    void criaEventosComDuracoesConfiguradas() {
        assertEquals(
                settings.clearDuration(),
                planner.plan(snapshot(WeatherTendency.TEMPO_LIMPO)).orElseThrow().duration()
        );
        assertEquals(
                settings.precipitationDuration(),
                planner.plan(snapshot(WeatherTendency.PRECIPITACAO)).orElseThrow().duration()
        );
        assertEquals(
                settings.stormDuration(),
                planner.plan(snapshot(WeatherTendency.TEMPESTADE)).orElseThrow().duration()
        );
    }

    @Test
    void rejeitaDuracaoNaoPositivaOuExcessiva() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new WeatherEventSettings(Duration.ZERO, Duration.ofMinutes(5), Duration.ofMinutes(3))
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new WeatherEventSettings(Duration.ofMinutes(31), Duration.ofMinutes(5), Duration.ofMinutes(3))
        );
    }

    private ClimateSnapshot snapshot(WeatherTendency tendency) {
        return new ClimateSnapshot(ThermalBand.TEMPERADO, MoistureBand.EQUILIBRADO, tendency);
    }
}
