package dev.signalshards.livingworld.features.climate.paper;

import dev.signalshards.livingworld.features.climate.application.ThermalEnvironmentContext;
import org.bukkit.entity.Player;

@FunctionalInterface
public interface PaperThermalEnvironmentProvider {
    ThermalEnvironmentContext forPlayer(Player player);
}
