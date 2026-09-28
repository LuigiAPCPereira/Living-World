package dev.signalshards.livingworld.core.companion;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourcePackSessionStoreTest {
    private final ResourcePackSessionStore store = new ResourcePackSessionStore();

    @Test
    void eventoSemRequestLivingWorldEhIgnorado() {
        assertTrue(store.apply(
                UUID.randomUUID(),
                UUID.randomUUID(),
                ResourcePackClientEvent.LOADED
        ).isEmpty());
    }

    @Test
    void requestNovoSubstituiSessaoAnteriorDoJogador() {
        UUID player = UUID.randomUUID();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();

        store.requested(player, first, false);
        store.requested(player, second, true);

        assertEquals(second, store.session(player).orElseThrow().requestId());
        assertTrue(store.session(player).orElseThrow().required());
        assertEquals(1, store.trackedPlayers());
    }

    @Test
    void quitResetEClearRemovemEstado() {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        store.requested(first, UUID.randomUUID(), false);
        store.requested(second, UUID.randomUUID(), false);

        store.reset(first);
        assertEquals(1, store.trackedPlayers());

        store.clear();
        assertEquals(0, store.trackedPlayers());
    }
}
