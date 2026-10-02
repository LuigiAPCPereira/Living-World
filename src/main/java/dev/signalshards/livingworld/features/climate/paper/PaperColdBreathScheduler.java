package dev.signalshards.livingworld.features.climate.paper;

import org.bukkit.plugin.Plugin;

import java.util.Objects;

@FunctionalInterface
public interface PaperColdBreathScheduler {
    void schedule(long delayTicks, Runnable action);

    static PaperColdBreathScheduler forPlugin(Plugin plugin) {
        Objects.requireNonNull(plugin, "plugin");
        return (delayTicks, action) -> {
            Objects.requireNonNull(action, "ação");
            if (delayTicks < 0L) {
                throw new IllegalArgumentException(
                        "O atraso do cold breath não pode ser negativo"
                );
            }
            if (delayTicks == 0L) {
                action.run();
                return;
            }
            plugin.getServer().getScheduler().runTaskLater(
                    plugin,
                    action,
                    delayTicks
            );
        };
    }
}
