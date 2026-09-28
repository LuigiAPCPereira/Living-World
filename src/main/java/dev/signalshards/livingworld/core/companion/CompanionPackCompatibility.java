package dev.signalshards.livingworld.core.companion;

public enum CompanionPackCompatibility {
    COMPATIBLE,
    OPTIONAL_MISSING,
    REQUIRED_MISSING,
    WRONG_KIND,
    SCHEMA_MISMATCH,
    HASH_MISMATCH;

    public boolean compatible() {
        return this == COMPATIBLE || this == OPTIONAL_MISSING;
    }
}
