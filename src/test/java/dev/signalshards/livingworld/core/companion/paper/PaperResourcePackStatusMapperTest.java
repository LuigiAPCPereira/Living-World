package dev.signalshards.livingworld.core.companion.paper;

import dev.signalshards.livingworld.core.companion.ResourcePackClientEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaperResourcePackStatusMapperTest {
    @Test
    void mapeiaTodosOsEstadosPublicosDoPaper() {
        assertEquals(
                ResourcePackClientEvent.ACCEPTED,
                PaperResourcePackStatusMapper.fromPaper(
                        PlayerResourcePackStatusEvent.Status.ACCEPTED
                )
        );
        assertEquals(
                ResourcePackClientEvent.DOWNLOADED,
                PaperResourcePackStatusMapper.fromPaper(
                        PlayerResourcePackStatusEvent.Status.DOWNLOADED
                )
        );
        assertEquals(
                ResourcePackClientEvent.LOADED,
                PaperResourcePackStatusMapper.fromPaper(
                        PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED
                )
        );
        assertEquals(
                ResourcePackClientEvent.DECLINED,
                PaperResourcePackStatusMapper.fromPaper(
                        PlayerResourcePackStatusEvent.Status.DECLINED
                )
        );
        assertEquals(
                ResourcePackClientEvent.FAILED_DOWNLOAD,
                PaperResourcePackStatusMapper.fromPaper(
                        PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD
                )
        );
        assertEquals(
                ResourcePackClientEvent.INVALID_URL,
                PaperResourcePackStatusMapper.fromPaper(
                        PlayerResourcePackStatusEvent.Status.INVALID_URL
                )
        );
        assertEquals(
                ResourcePackClientEvent.FAILED_RELOAD,
                PaperResourcePackStatusMapper.fromPaper(
                        PlayerResourcePackStatusEvent.Status.FAILED_RELOAD
                )
        );
        assertEquals(
                ResourcePackClientEvent.DISCARDED,
                PaperResourcePackStatusMapper.fromPaper(
                        PlayerResourcePackStatusEvent.Status.DISCARDED
                )
        );
    }
}
