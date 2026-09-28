package dev.signalshards.livingworld.core.companion.paper;

import dev.signalshards.livingworld.core.companion.ResourcePackClientEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

import java.util.Objects;

public final class PaperResourcePackStatusMapper {
    private PaperResourcePackStatusMapper() {
    }

    public static ResourcePackClientEvent fromPaper(
            PlayerResourcePackStatusEvent.Status status
    ) {
        Objects.requireNonNull(status, "status Paper");
        return switch (status) {
            case ACCEPTED -> ResourcePackClientEvent.ACCEPTED;
            case DOWNLOADED -> ResourcePackClientEvent.DOWNLOADED;
            case SUCCESSFULLY_LOADED -> ResourcePackClientEvent.LOADED;
            case DECLINED -> ResourcePackClientEvent.DECLINED;
            case FAILED_DOWNLOAD -> ResourcePackClientEvent.FAILED_DOWNLOAD;
            case INVALID_URL -> ResourcePackClientEvent.INVALID_URL;
            case FAILED_RELOAD -> ResourcePackClientEvent.FAILED_RELOAD;
            case DISCARDED -> ResourcePackClientEvent.DISCARDED;
        };
    }
}
