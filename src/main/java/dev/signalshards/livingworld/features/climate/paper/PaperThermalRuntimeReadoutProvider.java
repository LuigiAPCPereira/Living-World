package dev.signalshards.livingworld.features.climate.paper;

import dev.signalshards.livingworld.features.climate.application.PlayerThermalRuntimeService;
import dev.signalshards.livingworld.features.climate.application.ThermalExchangeComposer;
import dev.signalshards.livingworld.features.climate.application.ThermalExchangeContext;
import dev.signalshards.livingworld.features.climate.application.ThermalEnvironmentContext;
import dev.signalshards.livingworld.features.climate.application.ThermalRuntimeReadout;
import dev.signalshards.livingworld.features.climate.application.ThermalRuntimeReadoutProvider;
import dev.signalshards.livingworld.features.climate.domain.PlayerThermalPolicy;
import dev.signalshards.livingworld.features.climate.domain.PlayerThermalSnapshot;
import dev.signalshards.livingworld.features.climate.domain.ThermalFeedbackPolicy;
import org.bukkit.entity.Player;

import java.util.Objects;

/**
 * Diagnóstico bounded: observa o ambiente atual e combina com o snapshot runtime.
 */
public final class PaperThermalRuntimeReadoutProvider implements ThermalRuntimeReadoutProvider {
    private final PaperThermalEnvironmentProvider environmentProvider;
    private final PlayerThermalRuntimeService runtime;
    private final ThermalExchangeComposer composer;
    private final PlayerThermalPolicy thermalPolicy;
    private final ThermalFeedbackPolicy feedbackPolicy;

    public PaperThermalRuntimeReadoutProvider(
            PaperThermalEnvironmentProvider environmentProvider,
            PlayerThermalRuntimeService runtime
    ) {
        this(
                environmentProvider,
                runtime,
                new ThermalExchangeComposer(),
                new PlayerThermalPolicy(),
                new ThermalFeedbackPolicy()
        );
    }

    PaperThermalRuntimeReadoutProvider(
            PaperThermalEnvironmentProvider environmentProvider,
            PlayerThermalRuntimeService runtime,
            ThermalExchangeComposer composer,
            PlayerThermalPolicy thermalPolicy,
            ThermalFeedbackPolicy feedbackPolicy
    ) {
        this.environmentProvider = Objects.requireNonNull(
                environmentProvider,
                "resolver ambiental"
        );
        this.runtime = Objects.requireNonNull(runtime, "runtime térmico");
        this.composer = Objects.requireNonNull(composer, "compositor térmico");
        this.thermalPolicy = Objects.requireNonNull(thermalPolicy, "política térmica");
        this.feedbackPolicy = Objects.requireNonNull(
                feedbackPolicy,
                "política de feedback"
        );
    }

    @Override
    public ThermalRuntimeReadout snapshot(Player player) {
        Objects.requireNonNull(player, "jogador");
        ThermalEnvironmentContext environment = environmentProvider.forPlayer(player);
        PlayerThermalSnapshot body = runtime.snapshot(player.getUniqueId())
                .orElseGet(PlayerThermalSnapshot::neutral);
        var resolution = composer.resolve(ThermalExchangeContext.from(body, environment));
        var feedback = feedbackPolicy.profileFor(
                body.thermalState(),
                environment.ambientTemperature(),
                environment.activity(),
                environment.waterExposure()
        );

        return new ThermalRuntimeReadout(
                thermalPolicy.bandFor(body.thermalState()),
                body.thermalState().thermalLoad(),
                body.wetnessState().level(),
                environment.ambientTemperature().degreesCelsius(),
                environment.activity(),
                environment.waterExposure().submergedFraction(),
                environment.waterExposure().depthBlocks(),
                environment.precipitationExposure().level(),
                environment.directExposure(),
                environment.windExposure().level(),
                environment.shelterFactor().level(),
                environment.armorLoadout().pieces().size(),
                environment.localHeatExposures().size(),
                resolution.airRate().loadPerSecond(),
                resolution.waterRate().loadPerSecond(),
                resolution.activityRate().loadPerSecond(),
                resolution.localHeatRate().loadPerSecond(),
                resolution.directExposureRate().loadPerSecond(),
                resolution.netRate().loadPerSecond(),
                resolution.wetnessRate().levelPerSecond(),
                feedback
        );
    }
}
