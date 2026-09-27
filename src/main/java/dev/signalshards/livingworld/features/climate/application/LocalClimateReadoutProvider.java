package dev.signalshards.livingworld.features.climate.application;

import org.bukkit.entity.Player;

@FunctionalInterface
public interface LocalClimateReadoutProvider {
    LocalClimateReadout snapshot(Player player);
}
