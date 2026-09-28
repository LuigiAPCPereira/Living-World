package dev.signalshards.livingworld.core.companion;

public enum CompanionPackKind {
    DATAPACK("datapack"),
    RESOURCE_PACK("resourcepack");

    private final String manifestValue;

    CompanionPackKind(String manifestValue) {
        this.manifestValue = manifestValue;
    }

    public String manifestValue() {
        return manifestValue;
    }

    public static CompanionPackKind fromManifestValue(String value) {
        for (CompanionPackKind kind : values()) {
            if (kind.manifestValue.equalsIgnoreCase(value)) {
                return kind;
            }
        }
        throw new IllegalArgumentException(
                "Tipo de companion pack desconhecido: " + value
        );
    }
}
