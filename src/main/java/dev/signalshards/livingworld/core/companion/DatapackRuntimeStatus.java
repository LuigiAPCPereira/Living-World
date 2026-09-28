package dev.signalshards.livingworld.core.companion;

public enum DatapackRuntimeStatus {
    NOT_CONFIGURED,
    OPTIONAL_MISSING,
    REQUIRED_MISSING,
    INCOMPATIBLE,
    COMPATIBLE_DISABLED,
    COMPATIBLE_ENABLED;

    public boolean usable() {
        return this == NOT_CONFIGURED
                || this == COMPATIBLE_ENABLED
                || this == OPTIONAL_MISSING;
    }
}
