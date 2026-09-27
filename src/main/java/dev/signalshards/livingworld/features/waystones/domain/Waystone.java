package dev.signalshards.livingworld.features.waystones.domain;

import java.util.Objects;
import java.util.UUID;

public record Waystone(
        WaystoneId id,
        String name,
        UUID worldId,
        int x,
        int y,
        int z
) {
    private static final int MAX_NAME_LENGTH = 48;

    public Waystone {
        Objects.requireNonNull(id, "identificador da waystone");
        Objects.requireNonNull(name, "nome da waystone");
        Objects.requireNonNull(worldId, "identificador do mundo");

        name = name.strip();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("O nome da waystone não pode ficar vazio");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException(
                    "O nome da waystone não pode ultrapassar " + MAX_NAME_LENGTH + " caracteres"
            );
        }
    }
}
