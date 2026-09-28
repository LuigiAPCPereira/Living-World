package dev.signalshards.livingworld.core.companion;

import java.util.Objects;
import java.util.UUID;

/**
 * Estado por jogador de uma única requisição de resource pack.
 */
public record ResourcePackClientSession(
        UUID requestId,
        boolean required,
        ResourcePackClientStatus status
) {
    public ResourcePackClientSession {
        Objects.requireNonNull(requestId, "id da requisição");
        Objects.requireNonNull(status, "status do resource pack");
    }

    public static ResourcePackClientSession requested(
            UUID requestId,
            boolean required
    ) {
        return new ResourcePackClientSession(
                requestId,
                required,
                ResourcePackClientStatus.REQUESTED
        );
    }

    public boolean useVanillaFallback() {
        return !required && !status.presentationAvailable();
    }

    public boolean contractSatisfied() {
        if (status.presentationAvailable()) {
            return true;
        }
        return !required && status.terminal();
    }
}
