package dev.signalshards.livingworld.core.companion;

public enum DatapackRuntimeStatus {
    OPTIONAL_MISSING,
    REQUIRED_MISSING,
    INCOMPATIBLE,
    COMPATIBLE_DISABLED,
    COMPATIBLE_ENABLED;

    public boolean usable() {
        return this == COMPATIBLE_ENABLED || this == OPTIONAL_MISSING;
    }
}
