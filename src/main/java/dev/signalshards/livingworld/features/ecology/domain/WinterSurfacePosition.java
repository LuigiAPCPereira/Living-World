package dev.signalshards.livingworld.features.ecology.domain;

/**
 * Posição compacta dentro de um chunk. X/Z são locais 0..15; Y permanece
 * absoluto para suportar alturas negativas e futuras mudanças de build height.
 */
public record WinterSurfacePosition(
        int localX,
        int y,
        int localZ
) {
    public WinterSurfacePosition {
        validateLocal(localX, "x");
        validateLocal(localZ, "z");
    }

    public static WinterSurfacePosition fromWorld(
            int blockX,
            int blockY,
            int blockZ
    ) {
        return new WinterSurfacePosition(
                Math.floorMod(blockX, 16),
                blockY,
                Math.floorMod(blockZ, 16)
        );
    }

    public int packed() {
        return (y << 8) | (localZ << 4) | localX;
    }

    public static WinterSurfacePosition unpack(int packed) {
        return new WinterSurfacePosition(
                packed & 0xF,
                packed >> 8,
                (packed >>> 4) & 0xF
        );
    }

    private static void validateLocal(int value, String axis) {
        if (value < 0 || value > 15) {
            throw new IllegalArgumentException(
                    "Coordenada local " + axis + " deve ficar entre 0 e 15"
            );
        }
    }
}
