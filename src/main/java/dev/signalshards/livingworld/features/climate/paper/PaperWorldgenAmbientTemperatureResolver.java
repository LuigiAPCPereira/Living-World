package dev.signalshards.livingworld.features.climate.paper;

import dev.signalshards.livingworld.features.calendar.application.CalendarView;
import dev.signalshards.livingworld.features.climate.domain.AmbientTemperature;
import dev.signalshards.livingworld.features.climate.domain.AmbientTemperatureFactors;
import dev.signalshards.livingworld.features.climate.domain.AmbientTemperaturePolicy;
import dev.signalshards.livingworld.features.climate.domain.AmbientWeather;
import dev.signalshards.livingworld.features.climate.domain.EnvironmentalDimensionPolicy;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Objects;

/**
 * Temperatura ambiente multi-world baseada em propriedades observáveis.
 */
public final class PaperWorldgenAmbientTemperatureResolver
        implements PaperAmbientTemperatureProvider {
    private final CalendarView calendar;
    private final AmbientTemperaturePolicy temperaturePolicy;
    private final EnvironmentalDimensionPolicy dimensionPolicy;

    public PaperWorldgenAmbientTemperatureResolver(CalendarView calendar) {
        this(
                calendar,
                new AmbientTemperaturePolicy(),
                new EnvironmentalDimensionPolicy()
        );
    }

    PaperWorldgenAmbientTemperatureResolver(
            CalendarView calendar,
            AmbientTemperaturePolicy temperaturePolicy,
            EnvironmentalDimensionPolicy dimensionPolicy
    ) {
        this.calendar = Objects.requireNonNull(calendar, "calendário");
        this.temperaturePolicy = Objects.requireNonNull(
                temperaturePolicy,
                "policy de temperatura"
        );
        this.dimensionPolicy = Objects.requireNonNull(
                dimensionPolicy,
                "policy de dimensão"
        );
    }

    @Override
    public AmbientTemperature ambientTemperatureAt(Block block) {
        Objects.requireNonNull(block, "bloco");
        World world = block.getWorld();
        var dimension = PaperEnvironmentalDimensionMapper.fromPaper(
                world.getEnvironment()
        );
        var profile = dimensionPolicy.profileFor(dimension);
        AmbientWeather weather = profile.weatherFactor() == 0.0D
                ? AmbientWeather.CLEAR
                : PaperLocalClimateResolver.ambientWeather(
                        world.hasStorm(),
                        world.isThundering(),
                        world.getHumidity(
                                block.getX(),
                                block.getY(),
                                block.getZ()
                        )
                );

        return temperaturePolicy.temperature(
                world.getTemperature(
                        block.getX(),
                        block.getY(),
                        block.getZ()
                ),
                calendar.currentSeason(),
                calendar.currentSeasonProgress(),
                new AmbientTemperatureFactors(
                        Math.clamp(
                                block.getLightFromSky() / 15.0D,
                                0.0D,
                                1.0D
                        ),
                        (int) Math.floorMod(world.getTime(), 24_000L),
                        weather
                ),
                profile
        );
    }
}
