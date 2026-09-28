package dev.signalshards.livingworld.features.climate.application;

import dev.signalshards.livingworld.features.climate.domain.PlayerThermalPolicy;
import dev.signalshards.livingworld.features.climate.domain.PlayerThermalState;
import dev.signalshards.livingworld.features.climate.domain.WetnessPolicy;
import dev.signalshards.livingworld.features.climate.domain.WetnessState;

import java.time.Duration;
import java.util.Objects;

/**
 * Integra estado térmico + wetness em subpassos bounded.
 *
 * <p>O contexto ambiental permanece constante durante uma chamada, mas
 * thermal state e wetness são realimentados a cada subpasso. O catch-up
 * limitado evita trabalho proporcional a pausas longas do servidor.</p>
 */
public final class PlayerThermalSimulation {
    private final ThermalExchangeComposer composer;
    private final PlayerThermalPolicy thermalPolicy;
    private final WetnessPolicy wetnessPolicy;
    private final ThermalSimulationSettings settings;

    public PlayerThermalSimulation() {
        this(
                new ThermalExchangeComposer(),
                new PlayerThermalPolicy(),
                new WetnessPolicy(),
                ThermalSimulationSettings.livingWorldDefaults()
        );
    }

    PlayerThermalSimulation(
            ThermalExchangeComposer composer,
            PlayerThermalPolicy thermalPolicy,
            WetnessPolicy wetnessPolicy,
            ThermalSimulationSettings settings
    ) {
        this.composer = Objects.requireNonNull(composer, "compositor térmico");
        this.thermalPolicy = Objects.requireNonNull(thermalPolicy, "política térmica");
        this.wetnessPolicy = Objects.requireNonNull(wetnessPolicy, "política de wetness");
        this.settings = Objects.requireNonNull(settings, "configuração de simulação");
    }

    public ThermalSimulationResult advance(
            ThermalExchangeContext initialContext,
            Duration elapsed
    ) {
        Objects.requireNonNull(initialContext, "contexto térmico");
        Objects.requireNonNull(elapsed, "tempo decorrido");
        if (elapsed.isNegative()) {
            throw new IllegalArgumentException("O tempo decorrido não pode ser negativo");
        }
        if (elapsed.isZero()) {
            return new ThermalSimulationResult(
                    initialContext.thermalState(),
                    initialContext.wetness(),
                    Duration.ZERO,
                    Duration.ZERO
            );
        }

        Duration simulated = minimum(elapsed, settings.maximumCatchUp());
        Duration discarded = elapsed.minus(simulated);
        Duration remaining = simulated;
        PlayerThermalState thermalState = initialContext.thermalState();
        WetnessState wetnessState = initialContext.wetness();

        while (!remaining.isZero()) {
            Duration step = minimum(remaining, settings.maximumStep());
            ThermalExchangeContext stepContext = initialContext.withPlayerState(
                    thermalState,
                    wetnessState
            );
            ThermalExchangeResolution resolution = composer.resolve(stepContext);
            thermalState = thermalPolicy.advance(
                    thermalState,
                    resolution.netRate(),
                    step
            );
            wetnessState = wetnessPolicy.advance(
                    wetnessState,
                    resolution.wetnessRate(),
                    step
            );
            remaining = remaining.minus(step);
        }

        return new ThermalSimulationResult(
                thermalState,
                wetnessState,
                simulated,
                discarded
        );
    }

    private Duration minimum(Duration left, Duration right) {
        return left.compareTo(right) <= 0 ? left : right;
    }
}
