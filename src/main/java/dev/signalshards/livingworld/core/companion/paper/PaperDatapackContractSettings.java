package dev.signalshards.livingworld.core.companion.paper;

import java.util.Objects;

public record PaperDatapackContractSettings(
        boolean enabled,
        String paperName,
        boolean required
) {
    public PaperDatapackContractSettings {
        paperName = Objects.requireNonNull(
                paperName,
                "nome Paper do datapack"
        ).trim();
        if (enabled && paperName.isEmpty()) {
            throw new IllegalArgumentException(
                    "O nome Paper do datapack é obrigatório quando habilitado"
            );
        }
    }

    public static PaperDatapackContractSettings disabled() {
        return new PaperDatapackContractSettings(
                false,
                "",
                false
        );
    }
}
