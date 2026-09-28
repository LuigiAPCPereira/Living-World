package dev.signalshards.livingworld.core.companion;

import java.util.Objects;

/**
 * Contrato esperado pelo plugin para um companion pack.
 */
public record CompanionPackRequirement(
        CompanionPackKind kind,
        int schema,
        boolean required
) {
    public CompanionPackRequirement {
        Objects.requireNonNull(kind, "tipo requerido");
        if (schema <= 0) {
            throw new IllegalArgumentException(
                    "O schema requerido deve ser positivo"
            );
        }
    }
}
