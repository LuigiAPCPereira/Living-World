package dev.signalshards.livingworld.features.climate.paper;

import dev.signalshards.livingworld.features.climate.domain.AmbientTemperature;
import org.bukkit.block.Block;

@FunctionalInterface
public interface PaperAmbientTemperatureProvider {
    AmbientTemperature ambientTemperatureAt(Block block);
}
