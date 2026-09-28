package dev.signalshards.livingworld.core.companion;

/**
 * Estados do cliente espelhados do lifecycle público de resource pack do Paper.
 */
public enum ResourcePackClientStatus {
    NOT_REQUESTED,
    REQUESTED,
    ACCEPTED,
    DOWNLOADED,
    LOADED,
    DECLINED,
    FAILED_DOWNLOAD,
    INVALID_URL,
    FAILED_RELOAD,
    DISCARDED;

    public boolean terminal() {
        return switch (this) {
            case LOADED, DECLINED, FAILED_DOWNLOAD, INVALID_URL,
                    FAILED_RELOAD, DISCARDED -> true;
            default -> false;
        };
    }

    public boolean presentationAvailable() {
        return this == LOADED;
    }
}
