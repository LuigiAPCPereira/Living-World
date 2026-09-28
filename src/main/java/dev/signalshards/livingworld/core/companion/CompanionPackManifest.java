package dev.signalshards.livingworld.core.companion;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Manifest versionado de um companion pack do Living World.
 *
 * <p>A versão é informativa; compatibilidade comportamental usa schema.</p>
 */
public record CompanionPackManifest(
        CompanionPackKind kind,
        String version,
        int schema,
        Optional<String> sha256
) {
    public CompanionPackManifest {
        Objects.requireNonNull(kind, "tipo do pack");
        version = Objects.requireNonNull(version, "versão do pack").trim();
        if (version.isEmpty()) {
            throw new IllegalArgumentException(
                    "A versão do companion pack não pode ser vazia"
            );
        }
        if (schema <= 0) {
            throw new IllegalArgumentException(
                    "O schema do companion pack deve ser positivo"
            );
        }
        sha256 = Objects.requireNonNull(sha256, "hash opcional")
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(value -> value.toLowerCase(Locale.ROOT));
        sha256.ifPresent(CompanionPackManifest::validateSha256);
    }

    public static CompanionPackManifest withoutHash(
            CompanionPackKind kind,
            String version,
            int schema
    ) {
        return new CompanionPackManifest(
                kind,
                version,
                schema,
                Optional.empty()
        );
    }

    private static void validateSha256(String value) {
        if (value.length() != 64) {
            throw new IllegalArgumentException(
                    "SHA-256 deve possuir exatamente 64 caracteres hexadecimais"
            );
        }
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            boolean hexadecimal = (character >= '0' && character <= '9')
                    || (character >= 'a' && character <= 'f');
            if (!hexadecimal) {
                throw new IllegalArgumentException(
                        "SHA-256 deve conter apenas caracteres hexadecimais"
                );
            }
        }
    }
}
