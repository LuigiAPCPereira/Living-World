package dev.signalshards.livingworld.features.climate.application;

import org.bukkit.entity.Player;

@FunctionalInterface
public interface ThermalRuntimeReadoutProvider {
    ThermalRuntimeReadout snapshot(Player player);
}
