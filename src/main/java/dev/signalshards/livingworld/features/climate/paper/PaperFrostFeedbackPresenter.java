package dev.signalshards.livingworld.features.climate.paper;

import org.bukkit.entity.Player;

@FunctionalInterface
public interface PaperFrostFeedbackPresenter {
    void presentFrost(Player player, double intensity);
}
