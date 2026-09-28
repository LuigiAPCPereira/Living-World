package dev.signalshards.livingworld.features.climate.paper;

import dev.signalshards.livingworld.features.climate.domain.ArmorSlot;
import dev.signalshards.livingworld.features.climate.domain.ArmorThermalLoadout;
import dev.signalshards.livingworld.features.climate.domain.ArmorThermalMaterial;
import dev.signalshards.livingworld.features.climate.domain.EquippedArmorPiece;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Converte os quatro slots de armadura do Paper para o domínio térmico.
 */
public final class PaperArmorThermalResolver {
    private PaperArmorThermalResolver() {
    }

    public static ArmorThermalLoadout forPlayer(Player player) {
        Objects.requireNonNull(player, "jogador");
        PlayerInventory inventory = player.getInventory();
        List<EquippedArmorPiece> pieces = new ArrayList<>(4);
        addPiece(pieces, ArmorSlot.HEAD, inventory.getHelmet());
        addPiece(pieces, ArmorSlot.CHEST, inventory.getChestplate());
        addPiece(pieces, ArmorSlot.LEGS, inventory.getLeggings());
        addPiece(pieces, ArmorSlot.FEET, inventory.getBoots());
        return new ArmorThermalLoadout(pieces);
    }

    static Optional<ArmorThermalMaterial> materialFor(Material material) {
        Objects.requireNonNull(material, "material");
        return switch (material) {
            case LEATHER_HELMET, LEATHER_CHESTPLATE, LEATHER_LEGGINGS, LEATHER_BOOTS ->
                    Optional.of(ArmorThermalMaterial.LEATHER);
            case CHAINMAIL_HELMET, CHAINMAIL_CHESTPLATE, CHAINMAIL_LEGGINGS, CHAINMAIL_BOOTS ->
                    Optional.of(ArmorThermalMaterial.CHAINMAIL);
            case COPPER_HELMET, COPPER_CHESTPLATE, COPPER_LEGGINGS, COPPER_BOOTS ->
                    Optional.of(ArmorThermalMaterial.COPPER);
            case IRON_HELMET, IRON_CHESTPLATE, IRON_LEGGINGS, IRON_BOOTS ->
                    Optional.of(ArmorThermalMaterial.IRON);
            case GOLDEN_HELMET, GOLDEN_CHESTPLATE, GOLDEN_LEGGINGS, GOLDEN_BOOTS ->
                    Optional.of(ArmorThermalMaterial.GOLD);
            case DIAMOND_HELMET, DIAMOND_CHESTPLATE, DIAMOND_LEGGINGS, DIAMOND_BOOTS ->
                    Optional.of(ArmorThermalMaterial.DIAMOND);
            case NETHERITE_HELMET, NETHERITE_CHESTPLATE, NETHERITE_LEGGINGS, NETHERITE_BOOTS ->
                    Optional.of(ArmorThermalMaterial.NETHERITE);
            case TURTLE_HELMET -> Optional.of(ArmorThermalMaterial.TURTLE_SHELL);
            default -> Optional.empty();
        };
    }

    private static void addPiece(
            List<EquippedArmorPiece> pieces,
            ArmorSlot slot,
            ItemStack item
    ) {
        if (item == null || item.getType().isAir()) {
            return;
        }
        materialFor(item.getType()).ifPresent(material ->
                pieces.add(new EquippedArmorPiece(slot, material))
        );
    }
}
