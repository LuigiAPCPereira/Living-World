package dev.signalshards.livingworld.features.climate.paper;

import org.bukkit.entity.Player;

@FunctionalInterface
public interface PaperThermalFeedbackPresenter {
    void emitBreath(Player player, double intensity);
}
