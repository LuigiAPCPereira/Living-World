package dev.signalshards.livingworld.features.climate.paper;

import dev.signalshards.livingworld.features.climate.application.PlayerThermalRuntimeService;
import dev.signalshards.livingworld.features.climate.application.InMemoryThermalRuntimeReadoutStore;
import dev.signalshards.livingworld.features.climate.application.ThermalRuntimeReadout;
import dev.signalshards.livingworld.features.climate.application.ThermalRuntimeReadoutAssembler;
import dev.signalshards.livingworld.features.climate.application.ThermalRuntimeReadoutProvider;
import dev.signalshards.livingworld.features.climate.application.ThermalRuntimeReadoutStore;
import dev.signalshards.livingworld.features.climate.domain.PlayerThermalSnapshot;
import org.bukkit.entity.Player;

import java.util.Objects;

/**
 * Diagnóstico bounded: observa o ambiente atual e combina com o snapshot runtime.
 */
public final class PaperThermalRuntimeReadoutProvider implements ThermalRuntimeReadoutProvider {
    private final PaperThermalEnvironmentProvider environmentProvider;
    private final PlayerThermalRuntimeService runtime;
    private final ThermalRuntimeReadoutStore readouts;
    private final ThermalRuntimeReadoutAssembler assembler;

    public PaperThermalRuntimeReadoutProvider(
            PaperThermalEnvironmentProvider environmentProvider,
            PlayerThermalRuntimeService runtime
    ) {
        this(
                environmentProvider,
                runtime,
                new InMemoryThermalRuntimeReadoutStore()
        );
    }

    public PaperThermalRuntimeReadoutProvider(
            PaperThermalEnvironmentProvider environmentProvider,
            PlayerThermalRuntimeService runtime,
            ThermalRuntimeReadoutStore readouts
    ) {
        this(
                environmentProvider,
                runtime,
                readouts,
                new ThermalRuntimeReadoutAssembler()
        );
    }

    PaperThermalRuntimeReadoutProvider(
            PaperThermalEnvironmentProvider environmentProvider,
            PlayerThermalRuntimeService runtime,
            ThermalRuntimeReadoutStore readouts,
            ThermalRuntimeReadoutAssembler assembler
    ) {
        this.environmentProvider = Objects.requireNonNull(
                environmentProvider,
                "resolver ambiental"
        );
        this.runtime = Objects.requireNonNull(runtime, "runtime térmico");
        this.readouts = Objects.requireNonNull(readouts, "store de readout");
        this.assembler = Objects.requireNonNull(assembler, "assembler de readout");
    }

    @Override
    public ThermalRuntimeReadout snapshot(Player player) {
        Objects.requireNonNull(player, "jogador");
        var playerId = player.getUniqueId();
        return readouts.load(playerId).orElseGet(() -> bootstrap(player));
    }

    private ThermalRuntimeReadout bootstrap(Player player) {
        var playerId = player.getUniqueId();
        var environment = environmentProvider.forPlayer(player);
        PlayerThermalSnapshot body = runtime.snapshot(playerId)
                .orElseGet(PlayerThermalSnapshot::neutral);
        ThermalRuntimeReadout readout = assembler.assemble(body, environment);
        readouts.save(playerId, readout);
        return readout;
    }
}
