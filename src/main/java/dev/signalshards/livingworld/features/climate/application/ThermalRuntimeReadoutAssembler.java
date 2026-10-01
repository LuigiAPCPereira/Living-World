package dev.signalshards.livingworld.features.climate.application;

import dev.signalshards.livingworld.features.climate.domain.PlayerThermalPolicy;
import dev.signalshards.livingworld.features.climate.domain.PlayerThermalSnapshot;
import dev.signalshards.livingworld.features.climate.domain.ThermalFeedbackPolicy;

import java.util.Objects;

/**
 * Constrói o snapshot público a partir dos dados já resolvidos por um pulse.
 */
public final class ThermalRuntimeReadoutAssembler {
    private final ThermalExchangeComposer composer;
    private final PlayerThermalPolicy thermalPolicy;
    private final ThermalFeedbackPolicy feedbackPolicy;

    public ThermalRuntimeReadoutAssembler() {
        this(
                new ThermalExchangeComposer(),
                new PlayerThermalPolicy(),
                new ThermalFeedbackPolicy()
        );
    }

    public ThermalRuntimeReadoutAssembler(
            ThermalExchangeComposer composer,
            PlayerThermalPolicy thermalPolicy,
            ThermalFeedbackPolicy feedbackPolicy
    ) {
        this.composer = Objects.requireNonNull(composer, "compositor térmico");
        this.thermalPolicy = Objects.requireNonNull(
                thermalPolicy,
                "política térmica"
        );
        this.feedbackPolicy = Objects.requireNonNull(
                feedbackPolicy,
                "política de feedback"
        );
    }

    public ThermalRuntimeReadout assemble(
            PlayerThermalSnapshot body,
            ThermalEnvironmentContext environment
    ) {
        Objects.requireNonNull(body, "snapshot corporal");
        Objects.requireNonNull(environment, "ambiente térmico");

        var resolution = composer.resolve(
                ThermalExchangeContext.from(body, environment)
        );
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
