package dev.signalshards.livingworld.features.qol.autorefill.paper;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HotbarAutoRefillPolicyTest {
    private final HotbarAutoRefillPolicy policy = new HotbarAutoRefillPolicy();

    @Test
    void aceitaEsgotamentoDaStackEmpilhavelNoSlotSelecionado() {
        assertTrue(policy.shouldRefill(
                2,
                2,
                new ItemStack(Material.STONE, 1),
                new ItemStack(Material.AIR)
        ));
    }

    @Test
    void rejeitaSlotNaoSelecionadoStackAindaPresenteEFerramenta() {
        assertFalse(policy.shouldRefill(
                1,
                2,
                new ItemStack(Material.STONE, 1),
                new ItemStack(Material.AIR)
        ));
        assertFalse(policy.shouldRefill(
                2,
                2,
                new ItemStack(Material.STONE, 2),
                new ItemStack(Material.STONE, 1)
        ));
        assertFalse(policy.shouldRefill(
                2,
                2,
                new ItemStack(Material.DIAMOND_PICKAXE, 1),
                new ItemStack(Material.AIR)
        ));
    }

    @Test
    void rejeitaQuandoVanillaDeixaUmItemSubstitutoNoSlot() {
        assertFalse(policy.shouldRefill(
                0,
                0,
                new ItemStack(Material.HONEY_BOTTLE, 1),
                new ItemStack(Material.GLASS_BOTTLE, 1)
        ));
    }

    @Test
    void procuraSomenteSlotsPrincipaisEEscolhePrimeiraCorrespondenciaExata() {
        ItemStack[] storage = new ItemStack[36];
        storage[4] = new ItemStack(Material.STONE, 32);
        storage[9] = new ItemStack(Material.DIRT, 64);
        storage[17] = new ItemStack(Material.STONE, 12);
        storage[28] = new ItemStack(Material.STONE, 48);

        assertEquals(
                17,
                policy.findReplacementSlot(
                        storage,
                        new ItemStack(Material.STONE, 1)
                )
        );
    }

    @Test
    void naoAceitaMaterialDiferenteNemHotbarComoFonte() {
        ItemStack[] storage = new ItemStack[36];
        storage[1] = new ItemStack(Material.STONE, 64);
        storage[9] = new ItemStack(Material.DIRT, 64);

        assertEquals(
                -1,
                policy.findReplacementSlot(
                        storage,
                        new ItemStack(Material.STONE, 1)
                )
        );
    }
}
