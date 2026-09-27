package dev.signalshards.livingworld.features.waystones.paper;

final class WaystoneMenuLayout {
    static final int MAX_ENTRIES = 54;

    private WaystoneMenuLayout() {
    }

    static boolean canDisplay(int count) {
        return count >= 0 && count <= MAX_ENTRIES;
    }

    static int inventorySize(int count) {
        if (count < 1 || count > MAX_ENTRIES) {
            throw new IllegalArgumentException(
                    "O menu suporta entre 1 e " + MAX_ENTRIES + " waystones"
            );
        }
        return ((count + 8) / 9) * 9;
    }
}
