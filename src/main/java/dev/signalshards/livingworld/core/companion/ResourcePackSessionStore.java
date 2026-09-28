package dev.signalshards.livingworld.core.companion;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Estado efêmero de resource pack por jogador.
 */
public final class ResourcePackSessionStore {
    private final ResourcePackClientPolicy policy;
    private final Map<UUID, ResourcePackClientSession> sessions = new HashMap<>();

    public ResourcePackSessionStore() {
        this(new ResourcePackClientPolicy());
    }

    ResourcePackSessionStore(ResourcePackClientPolicy policy) {
        this.policy = Objects.requireNonNull(policy, "policy de resource pack");
    }

    public ResourcePackClientSession requested(
            UUID playerId,
            UUID requestId,
            boolean required
    ) {
        Objects.requireNonNull(playerId, "id do jogador");
        ResourcePackClientSession session =
                ResourcePackClientSession.requested(requestId, required);
        sessions.put(playerId, session);
        return session;
    }

    public Optional<ResourcePackClientSession> apply(
            UUID playerId,
            UUID eventRequestId,
            ResourcePackClientEvent event
    ) {
        Objects.requireNonNull(playerId, "id do jogador");
        ResourcePackClientSession current = sessions.get(playerId);
        if (current == null) {
            return Optional.empty();
        }
        ResourcePackClientSession updated = policy.apply(
                current,
                eventRequestId,
                event
        );
        sessions.put(playerId, updated);
        return Optional.of(updated);
    }

    public Optional<ResourcePackClientSession> session(UUID playerId) {
        return Optional.ofNullable(sessions.get(
                Objects.requireNonNull(playerId, "id do jogador")
        ));
    }

    public void reset(UUID playerId) {
        sessions.remove(Objects.requireNonNull(playerId, "id do jogador"));
    }

    public void clear() {
        sessions.clear();
    }

    public int trackedPlayers() {
        return sessions.size();
    }
}
