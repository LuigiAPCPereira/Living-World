package dev.signalshards.livingworld.features.ecology.paper;

import dev.signalshards.livingworld.features.ecology.domain.PhysicalWinterSettings;

import java.util.Objects;

public record PaperPhysicalWinterSettings(
        boolean enabled,
        long updatePeriodTicks,
        int probesPerPlayer,
        int maxMutationsPerPlayer,
        int radiusBlocks,
        PhysicalWinterSettings domain
) {
    public PaperPhysicalWinterSettings {
        if (updatePeriodTicks <= 0L) {
            throw new IllegalArgumentException(
                    "O período do inverno físico deve ser positivo"
            );
        }
        if (probesPerPlayer <= 0) {
            throw new IllegalArgumentException(
                    "O orçamento de probes deve ser positivo"
            );
        }
        if (maxMutationsPerPlayer <= 0
                || maxMutationsPerPlayer > probesPerPlayer) {
            throw new IllegalArgumentException(
                    "O orçamento de mutations deve ficar entre 1 e probes"
            );
        }
        if (radiusBlocks <= 0) {
            throw new IllegalArgumentException(
                    "O raio do inverno físico deve ser positivo"
            );
        }
        Objects.requireNonNull(domain, "configuração térmica do inverno");
    }

    public static PaperPhysicalWinterSettings defaults() {
        return new PaperPhysicalWinterSettings(
                false,
                40L,
                4,
                1,
                8,
                PhysicalWinterSettings.defaults()
        );
    }
}
