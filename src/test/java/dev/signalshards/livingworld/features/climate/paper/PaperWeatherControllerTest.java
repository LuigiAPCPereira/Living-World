package dev.signalshards.livingworld.features.climate.paper;

import dev.signalshards.livingworld.features.climate.application.WeatherEventPlan;
import dev.signalshards.livingworld.features.climate.domain.WeatherTendency;
import org.bukkit.World;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaperWeatherControllerTest {
    @Test
    void aplicaPrecipitacaoComDuracaoLimitada() {
        List<String> calls = new ArrayList<>();
        World world = recordingWorld(calls);
        PaperWeatherController controller = new PaperWeatherController();

        controller.apply(
                world,
                new WeatherEventPlan(WeatherTendency.PRECIPITACAO, Duration.ofMinutes(5))
        );

        assertEquals(List.of(
                "setThundering:false",
                "setStorm:true",
                "setWeatherDuration:6000"
        ), calls);
    }

    @Test
    void aplicaTempestadeComChuvaETrovoesNaMesmaDuracao() {
        List<String> calls = new ArrayList<>();
        World world = recordingWorld(calls);
        PaperWeatherController controller = new PaperWeatherController();

        controller.apply(
                world,
                new WeatherEventPlan(WeatherTendency.TEMPESTADE, Duration.ofMinutes(3))
        );

        assertEquals(List.of(
                "setStorm:true",
                "setWeatherDuration:3600",
                "setThundering:true",
                "setThunderDuration:3600"
        ), calls);
    }

    private World recordingWorld(List<String> calls) {
        return (World) Proxy.newProxyInstance(
                World.class.getClassLoader(),
                new Class<?>[]{World.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("getName")) {
                        return "mundo-teste";
                    }
                    if (method.getName().startsWith("set")) {
                        calls.add(method.getName() + ":" + args[0]);
                    }
                    return null;
                }
        );
    }
}
