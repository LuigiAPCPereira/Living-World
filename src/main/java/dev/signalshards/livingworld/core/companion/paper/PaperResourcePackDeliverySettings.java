package dev.signalshards.livingworld.core.companion.paper;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Objects;

public record PaperResourcePackDeliverySettings(
        boolean enabled,
        String url,
        String sha1,
        boolean required,
        String prompt
) {
    public PaperResourcePackDeliverySettings {
        url = Objects.requireNonNull(url, "URL do resource pack").trim();
        sha1 = Objects.requireNonNull(sha1, "SHA-1 do resource pack")
                .trim()
                .toLowerCase(Locale.ROOT);
        prompt = Objects.requireNonNull(prompt, "prompt do resource pack").trim();
        if (enabled) {
            validateUrl(url);
            validateSha1(sha1);
        }
    }

    public static PaperResourcePackDeliverySettings disabled() {
        return new PaperResourcePackDeliverySettings(
                false,
                "",
                "",
                false,
                "Living World environmental visuals"
        );
    }

    private static void validateUrl(String value) {
        try {
            URI uri = new URI(value);
            String scheme = uri.getScheme();
            if (scheme == null
                    || (!scheme.equalsIgnoreCase("https")
                    && !scheme.equalsIgnoreCase("http"))) {
                throw new IllegalArgumentException(
                        "Resource pack URL deve usar http ou https"
                );
            }
        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException(
                    "Resource pack URL inválida",
                    exception
            );
        }
    }

    private static void validateSha1(String value) {
        if (value.length() != 40) {
            throw new IllegalArgumentException(
                    "Resource pack SHA-1 deve possuir 40 caracteres hexadecimais"
            );
        }
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            boolean hexadecimal = (character >= '0' && character <= '9')
                    || (character >= 'a' && character <= 'f');
            if (!hexadecimal) {
                throw new IllegalArgumentException(
                        "Resource pack SHA-1 deve ser hexadecimal"
                );
            }
        }
    }
}
