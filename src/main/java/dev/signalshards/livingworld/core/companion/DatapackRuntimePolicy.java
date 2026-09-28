package dev.signalshards.livingworld.core.companion;

import java.util.Objects;

/**
 * Combina contrato do manifest com o estado enabled observado no Paper.
 */
public final class DatapackRuntimePolicy {
    public DatapackRuntimeStatus statusFor(
            CompanionPackCompatibility compatibility,
            boolean enabled
    ) {
        Objects.requireNonNull(compatibility, "compatibilidade");
        return switch (compatibility) {
            case OPTIONAL_MISSING -> DatapackRuntimeStatus.OPTIONAL_MISSING;
            case REQUIRED_MISSING -> DatapackRuntimeStatus.REQUIRED_MISSING;
            case COMPATIBLE -> enabled
                    ? DatapackRuntimeStatus.COMPATIBLE_ENABLED
                    : DatapackRuntimeStatus.COMPATIBLE_DISABLED;
            case WRONG_KIND, SCHEMA_MISMATCH, HASH_MISMATCH ->
                    DatapackRuntimeStatus.INCOMPATIBLE;
        };
    }
}
