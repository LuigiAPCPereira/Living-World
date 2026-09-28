package dev.signalshards.livingworld.core.companion;

/**
 * Schemas esperados pelo plugin atual.
 *
 * <p>Ambos são opcionais até o bootstrap/delivery de LW-124 ser implementado.</p>
 */
public final class LivingWorldCompanionContracts {
    public static final int DATAPACK_SCHEMA = 1;
    public static final int RESOURCE_PACK_SCHEMA = 1;

    private LivingWorldCompanionContracts() {
    }

    public static CompanionPackRequirement datapack() {
        return new CompanionPackRequirement(
                CompanionPackKind.DATAPACK,
                DATAPACK_SCHEMA,
                false
        );
    }

    public static CompanionPackRequirement resourcePack() {
        return new CompanionPackRequirement(
                CompanionPackKind.RESOURCE_PACK,
                RESOURCE_PACK_SCHEMA,
                false
        );
    }
}
