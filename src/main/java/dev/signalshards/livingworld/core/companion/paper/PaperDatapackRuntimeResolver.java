package dev.signalshards.livingworld.core.companion.paper;

import dev.signalshards.livingworld.core.companion.CompanionPackCompatibility;
import dev.signalshards.livingworld.core.companion.DatapackRuntimePolicy;
import dev.signalshards.livingworld.core.companion.DatapackRuntimeStatus;
import io.papermc.paper.datapack.Datapack;
import io.papermc.paper.datapack.DatapackManager;

import java.util.Objects;

/**
 * Lê somente o estado enabled do datapack já conhecido pelo Paper.
 */
public final class PaperDatapackRuntimeResolver {
    private final DatapackRuntimePolicy policy;

    public PaperDatapackRuntimeResolver() {
        this(new DatapackRuntimePolicy());
    }

    PaperDatapackRuntimeResolver(DatapackRuntimePolicy policy) {
        this.policy = Objects.requireNonNull(policy, "policy de datapack");
    }

    public DatapackRuntimeStatus resolve(
            DatapackManager manager,
            String packName,
            CompanionPackCompatibility compatibility
    ) {
        Objects.requireNonNull(manager, "datapack manager");
        Objects.requireNonNull(packName, "nome do datapack");
        Objects.requireNonNull(compatibility, "compatibilidade");
        if (packName.isBlank()) {
            throw new IllegalArgumentException(
                    "O nome do datapack não pode ser vazio"
            );
        }

        if (compatibility != CompanionPackCompatibility.COMPATIBLE) {
            return policy.statusFor(compatibility, false);
        }

        Datapack pack = manager.getPack(packName);
        if (pack == null) {
            return DatapackRuntimeStatus.OPTIONAL_MISSING;
        }
        return policy.statusFor(
                CompanionPackCompatibility.COMPATIBLE,
                pack.isEnabled()
        );
    }
}
