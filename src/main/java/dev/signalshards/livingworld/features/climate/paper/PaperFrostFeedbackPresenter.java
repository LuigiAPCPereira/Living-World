package dev.signalshards.livingworld.features.climate.paper;

import org.bukkit.entity.Player;

@FunctionalInterface
public interface PaperFrostFeedbackPresenter {
    void presentFrost(Player player, double intensity);

    default void clearFrost(Player player) {
        // Optional lifecycle hook for presenters that own persistent visual state.
    }
}
