package dev.signalshards.livingworld.features.discovery.domain;

import java.util.Objects;

/**
 * Tipo da coisa descoberta.
 *
 * <p>O vocabulário é aberto de propósito: cada capacidade registra o seu próprio tipo
 * sem que o núcleo do Discovery conheça as features que o produzem. O valor é
 * normalizado em minúsculas e validado porque ele participa da persistência.
 */
public record DiscoveryType(String value) {
    private static final int MAX_LENGTH = 32;
    public DiscoveryType {
        Objects.requireNonNull(value, "tipo de descoberta");
        value = value.strip();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("O tipo de descoberta não pode ficar vazio");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "O tipo de descoberta não pode ultrapassar " + MAX_LENGTH + " caracteres"
            );
        }
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            boolean valid = (character >= 'a' && character <= 'z')
                    || (character >= '0' && character <= '9')
                    || character == '_'
                    || character == '-';
            if (!valid) {
                throw new IllegalArgumentException(
                        "O tipo de descoberta aceita apenas minúsculas, números, '_' e '-': " + value
                );
            }
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
