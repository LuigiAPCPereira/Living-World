package dev.signalshards.livingworld.features.waystones.paper;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaperWaystoneSettingsLoaderTest {
    @Test
    void usaLodestoneComoAncoraPadrao() {
        PaperWaystoneSettings settings = PaperWaystoneSettingsLoader.load(
                new YamlConfiguration(),
                this::resolveMaterial,
                material -> true
        );

        assertEquals(Material.LODESTONE, settings.anchorMaterial());
        assertEquals(6.0D, settings.renameMaxDistance());
    }

    @Test
    void aceitaOutroBlocoConfigurado() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("waystones.anchor-material", "RESPAWN_ANCHOR");

        assertEquals(
                Material.RESPAWN_ANCHOR,
                PaperWaystoneSettingsLoader.load(
                        config,
                        this::resolveMaterial,
                        material -> true
                ).anchorMaterial()
        );
    }

    @Test
    void rejeitaMaterialDesconhecido() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("waystones.anchor-material", "BLOCO_QUE_NAO_EXISTE");

        assertThrows(
                IllegalArgumentException.class,
                () -> PaperWaystoneSettingsLoader.load(
                        config,
                        this::resolveMaterial,
                        material -> true
                )
        );
    }

    @Test
    void rejeitaMaterialQueNaoPassaNaPoliticaDeAncora() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("waystones.anchor-material", "LODESTONE");

        assertThrows(
                IllegalArgumentException.class,
                () -> PaperWaystoneSettingsLoader.load(
                        config,
                        this::resolveMaterial,
                        material -> false
                )
        );
    }

    @Test
    void carregaDistanciaDeRenameConfigurada() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("waystones.rename-max-distance", 8.5D);

        assertEquals(
                8.5D,
                PaperWaystoneSettingsLoader.load(
                        config,
                        this::resolveMaterial,
                        material -> true
                ).renameMaxDistance()
        );
    }

    @Test
    void rejeitaDistanciaDeRenameInvalida() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("waystones.rename-max-distance", 0.5D);

        assertThrows(
                IllegalArgumentException.class,
                () -> PaperWaystoneSettingsLoader.load(
                        config,
                        this::resolveMaterial,
                        material -> true
                )
        );
    }

    private Material resolveMaterial(String name) {
        return Map.of(
                "LODESTONE", Material.LODESTONE,
                "RESPAWN_ANCHOR", Material.RESPAWN_ANCHOR
        ).get(name);
    }
}
