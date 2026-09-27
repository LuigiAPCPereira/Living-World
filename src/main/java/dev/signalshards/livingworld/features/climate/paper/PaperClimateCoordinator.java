package dev.signalshards.livingworld.features.climate.paper;

import dev.signalshards.livingworld.core.i18n.MessageCatalog;
import dev.signalshards.livingworld.features.calendar.application.CalendarProgress;
import dev.signalshards.livingworld.features.calendar.application.CalendarProgressListener;
import dev.signalshards.livingworld.features.climate.application.WeatherEventPlan;
import dev.signalshards.livingworld.features.climate.application.WeatherEventPlanner;
import dev.signalshards.livingworld.features.climate.domain.ClimatePolicy;
import dev.signalshards.livingworld.features.climate.domain.ClimateState;
import dev.signalshards.livingworld.features.seasons.domain.Season;
import dev.signalshards.livingworld.features.seasons.domain.SeasonCycle;
import org.bukkit.World;

import java.util.Objects;
import java.util.logging.Logger;

/**
 * Coordena a reação climática diária sem criar um barramento genérico.
 *
 * <p>Mesmo que o calendário avance vários dias de uma vez, a integração avalia
 * apenas o estado final e aplica no máximo um evento climático, evitando rajadas
 * de efeitos após lag ou grandes saltos válidos.</p>
 */
public final class PaperClimateCoordinator implements CalendarProgressListener {
    private final World world;
    private final PaperClimateSampler sampler;
    private final ClimatePolicy policy;
    private final WeatherEventPlanner planner;
    private final PaperWeatherController controller;
    private final SeasonCycle seasons;
    private final MessageCatalog messages;
    private final Logger logger;

    private final ClimateState climateState = ClimateState.stable();

    public PaperClimateCoordinator(
            World world,
            PaperClimateSampler sampler,
            ClimatePolicy policy,
            WeatherEventPlanner planner,
            PaperWeatherController controller,
            SeasonCycle seasons,
            MessageCatalog messages,
            Logger logger
    ) {
        this.world = Objects.requireNonNull(world, "mundo");
        this.sampler = Objects.requireNonNull(sampler, "amostrador climático");
        this.policy = Objects.requireNonNull(policy, "política climática");
        this.planner = Objects.requireNonNull(planner, "planejador de eventos");
        this.controller = Objects.requireNonNull(controller, "controlador de clima Paper");
        this.seasons = Objects.requireNonNull(seasons, "ciclo de estações");
        this.messages = Objects.requireNonNull(messages, "catálogo de mensagens");
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    @Override
    public void onProgress(CalendarProgress progress) {
        Season season = seasons.seasonFor(progress.currentDate());
        var profile = sampler.sample(world);
        var snapshot = policy.evaluate(profile, season, climateState);

        planner.plan(snapshot).ifPresent(this::apply);
    }

    private void apply(WeatherEventPlan plan) {
        controller.apply(world, plan);
        logger.info(messages.text(
                "climate.weather-applied",
                world.getName(),
                plan.tendency(),
                plan.duration().toSeconds()
        ));
    }
}
