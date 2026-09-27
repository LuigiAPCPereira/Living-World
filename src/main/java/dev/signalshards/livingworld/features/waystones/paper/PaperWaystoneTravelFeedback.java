package dev.signalshards.livingworld.features.waystones.paper;

import dev.signalshards.livingworld.core.i18n.MessageCatalog;
import dev.signalshards.livingworld.features.waystones.application.WaystoneTravelResult;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

final class PaperWaystoneTravelFeedback {
    private PaperWaystoneTravelFeedback() {
    }

    static void sendStart(
            Player player,
            MessageCatalog messages,
            String name
    ) {
        player.sendMessage(messages.component(
                NamedTextColor.AQUA,
                "waystone.travel-start",
                name
        ));
    }

    static void sendResult(
            Player player,
            MessageCatalog messages,
            String name,
            WaystoneTravelResult result
    ) {
        if (!player.isOnline()) {
            return;
        }

        StyledResult styled = switch (result) {
            case SUCCESS -> new StyledResult(
                    "waystone.travel-success",
                    NamedTextColor.GREEN
            );
            case NOT_ACTIVATED -> new StyledResult(
                    "waystone.travel-not-activated",
                    NamedTextColor.RED
            );
            case WORLD_UNAVAILABLE -> new StyledResult(
                    "waystone.travel-world-unavailable",
                    NamedTextColor.RED
            );
            case DESTINATION_UNSAFE -> new StyledResult(
                    "waystone.travel-unsafe",
                    NamedTextColor.YELLOW
            );
            case PLAYER_OFFLINE -> null;
            case TELEPORT_REJECTED -> new StyledResult(
                    "waystone.travel-rejected",
                    NamedTextColor.RED
            );
        };
        if (styled != null) {
            player.sendMessage(messages.component(
                    styled.color(),
                    styled.key(),
                    name
            ));
        }
    }

    private record StyledResult(String key, NamedTextColor color) {
    }
}
