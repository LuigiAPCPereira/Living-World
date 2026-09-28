package dev.signalshards.livingworld.features.climate.paper;

import dev.signalshards.livingworld.features.climate.domain.EnvironmentalDimension;
import org.bukkit.World;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaperEnvironmentalDimensionMapperTest {
    @Test
    void mapeiaAmbientesPaperSemConsultarNomeDoMundoOuBiome() {
        assertEquals(
                EnvironmentalDimension.OVERWORLD,
                PaperEnvironmentalDimensionMapper.fromPaper(
                        World.Environment.NORMAL
                )
        );
        assertEquals(
                EnvironmentalDimension.NETHER,
                PaperEnvironmentalDimensionMapper.fromPaper(
                        World.Environment.NETHER
                )
        );
        assertEquals(
                EnvironmentalDimension.END,
                PaperEnvironmentalDimensionMapper.fromPaper(
                        World.Environment.THE_END
                )
        );
        assertEquals(
                EnvironmentalDimension.CUSTOM,
                PaperEnvironmentalDimensionMapper.fromPaper(
                        World.Environment.CUSTOM
                )
        );
    }
}
