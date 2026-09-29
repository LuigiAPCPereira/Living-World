package dev.signalshards.livingworld.features.ecology.paper;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaperPhysicalSnowSettingsLoaderTest {
    @Test
    void defaultUsaQuatroLayers() {
        assertEquals(
                4,
                PaperPhysicalSnowSettingsLoader.load(
                        new YamlConfiguration()
                ).maxLayers()
        );
    }

    @Test
    void carregaCapConfigurado() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("ecology.physical-winter.max-snow-layers", 6);

        assertEquals(
                6,
                PaperPhysicalSnowSettingsLoader.load(config).maxLayers()
        );
    }

    @Test
    void rejeitaCapForaDoVanilla() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("ecology.physical-winter.max-snow-layers", 9);

        assertThrows(
                IllegalArgumentException.class,
                () -> PaperPhysicalSnowSettingsLoader.load(config)
        );
    }
}
