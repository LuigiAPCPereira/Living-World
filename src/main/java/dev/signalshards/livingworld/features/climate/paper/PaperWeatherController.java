package dev.signalshards.livingworld.features.climate.paper;

import dev.signalshards.livingworld.features.climate.application.WeatherEventPlan;
import org.bukkit.World;

import java.time.Duration;
import java.util.Objects;

/**
 * Única fronteira responsável por traduzir um plano climático para a API do Paper.
 */
public final class PaperWeatherController {
    private static final long MILLIS_PER_TICK = 50L;

    public void apply(World world, WeatherEventPlan plan) {
        Objects.requireNonNull(world, "mundo");
        Objects.requireNonNull(plan, "plano climático");

        int durationTicks = toTicks(plan.duration());

        switch (plan.tendency()) {
            case ESTAVEL -> throw new IllegalArgumentException(
                    "Um plano climático não deve ser criado para estado estável"
            );
            case TEMPO_LIMPO -> applyClearWeather(world, durationTicks);
            case PRECIPITACAO -> applyPrecipitation(world, durationTicks);
            case TEMPESTADE -> applyStorm(world, durationTicks);
        }

    }

    private void applyClearWeather(World world, int durationTicks) {
        world.setThundering(false);
        world.setStorm(false);
        world.setClearWeatherDuration(durationTicks);
    }

    private void applyPrecipitation(World world, int durationTicks) {
        world.setThundering(false);
        world.setStorm(true);
        world.setWeatherDuration(durationTicks);
    }

    private void applyStorm(World world, int durationTicks) {
        world.setStorm(true);
        world.setWeatherDuration(durationTicks);
        world.setThundering(true);
        world.setThunderDuration(durationTicks);
    }

    private int toTicks(Duration duration) {
        long ticks = Math.max(1L, duration.toMillis() / MILLIS_PER_TICK);
        return Math.toIntExact(ticks);
    }
}
