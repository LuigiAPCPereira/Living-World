package dev.signalshards.livingworld.features.ecology.domain;

public enum WinterSurfaceKind {
    ICE(1),
    SNOW(2);

    private final int persistentId;

    WinterSurfaceKind(int persistentId) {
        this.persistentId = persistentId;
    }

    public int persistentId() {
        return persistentId;
    }

    public static WinterSurfaceKind fromPersistentId(int id) {
        for (WinterSurfaceKind kind : values()) {
            if (kind.persistentId == id) {
                return kind;
            }
        }
        throw new IllegalArgumentException(
                "Tipo de superfície de inverno desconhecido: " + id
        );
    }
}
