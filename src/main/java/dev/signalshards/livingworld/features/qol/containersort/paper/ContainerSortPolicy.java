package dev.signalshards.livingworld.features.qol.containersort.paper;

final class ContainerSortPolicy {
    boolean shouldTrigger(
            boolean supportedStorage,
            boolean clickedTopInventory,
            boolean shiftLeftClick,
            boolean vanillaNoOp,
            boolean clickedSlotEmpty,
            boolean cursorEmpty,
            boolean exclusiveViewer,
            boolean excludedGameMode
    ) {
        return supportedStorage
                && clickedTopInventory
                && shiftLeftClick
                && vanillaNoOp
                && clickedSlotEmpty
                && cursorEmpty
                && exclusiveViewer
                && !excludedGameMode;
    }
}
