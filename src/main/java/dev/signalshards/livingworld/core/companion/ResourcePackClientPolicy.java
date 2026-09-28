package dev.signalshards.livingworld.core.companion;

import java.util.Objects;
import java.util.UUID;

/**
 * State machine de resource pack por request ID.
 */
public final class ResourcePackClientPolicy {
    public ResourcePackClientSession apply(
            ResourcePackClientSession current,
            UUID eventRequestId,
            ResourcePackClientEvent event
    ) {
        Objects.requireNonNull(current, "sessão atual");
        Objects.requireNonNull(eventRequestId, "id do evento");
        Objects.requireNonNull(event, "evento do resource pack");

        if (!current.requestId().equals(eventRequestId)) {
            return current;
        }
        if (current.status().terminal()) {
            return current;
        }

        ResourcePackClientStatus next = switch (event) {
            case ACCEPTED -> ResourcePackClientStatus.ACCEPTED;
            case DOWNLOADED -> ResourcePackClientStatus.DOWNLOADED;
            case LOADED -> ResourcePackClientStatus.LOADED;
            case DECLINED -> ResourcePackClientStatus.DECLINED;
            case FAILED_DOWNLOAD -> ResourcePackClientStatus.FAILED_DOWNLOAD;
            case INVALID_URL -> ResourcePackClientStatus.INVALID_URL;
            case FAILED_RELOAD -> ResourcePackClientStatus.FAILED_RELOAD;
            case DISCARDED -> ResourcePackClientStatus.DISCARDED;
        };

        if (!transitionAllowed(current.status(), next)) {
            return current;
        }
        return new ResourcePackClientSession(
                current.requestId(),
                current.required(),
                next
        );
    }

    private boolean transitionAllowed(
            ResourcePackClientStatus current,
            ResourcePackClientStatus next
    ) {
        if (next == ResourcePackClientStatus.DECLINED
                || next == ResourcePackClientStatus.FAILED_DOWNLOAD
                || next == ResourcePackClientStatus.INVALID_URL
                || next == ResourcePackClientStatus.FAILED_RELOAD
                || next == ResourcePackClientStatus.DISCARDED) {
            return true;
        }
        return switch (current) {
            case REQUESTED -> next == ResourcePackClientStatus.ACCEPTED
                    || next == ResourcePackClientStatus.DOWNLOADED
                    || next == ResourcePackClientStatus.LOADED;
            case ACCEPTED -> next == ResourcePackClientStatus.DOWNLOADED
                    || next == ResourcePackClientStatus.LOADED;
            case DOWNLOADED -> next == ResourcePackClientStatus.LOADED;
            default -> false;
        };
    }
}
