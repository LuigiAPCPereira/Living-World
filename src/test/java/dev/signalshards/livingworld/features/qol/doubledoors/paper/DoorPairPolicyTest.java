package dev.signalshards.livingworld.features.qol.doubledoors.paper;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Door;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DoorPairPolicyTest {
    private final DoorPairPolicy policy = new DoorPairPolicy();

    @Test
    void aceitaMesmoMaterialFacingEHingesOpostos() {
        Door left = door(BlockFace.NORTH, Door.Hinge.LEFT, false);
        Door right = door(BlockFace.NORTH, Door.Hinge.RIGHT, false);

        assertTrue(policy.isCompatible(
                Material.OAK_DOOR,
                left,
                Material.OAK_DOOR,
                right
        ));
    }

    @Test
    void rejeitaMesmoHingeMaterialDiferenteOuRedstone() {
        Door left = door(BlockFace.NORTH, Door.Hinge.LEFT, false);

        assertFalse(policy.isCompatible(
                Material.OAK_DOOR,
                left,
                Material.OAK_DOOR,
                door(BlockFace.NORTH, Door.Hinge.LEFT, false)
        ));
        assertFalse(policy.isCompatible(
                Material.OAK_DOOR,
                left,
                Material.SPRUCE_DOOR,
                door(BlockFace.NORTH, Door.Hinge.RIGHT, false)
        ));
        assertFalse(policy.isCompatible(
                Material.OAK_DOOR,
                left,
                Material.OAK_DOOR,
                door(BlockFace.NORTH, Door.Hinge.RIGHT, true)
        ));
    }

    private Door door(BlockFace facing, Door.Hinge hinge, boolean powered) {
        return (Door) Proxy.newProxyInstance(
                Door.class.getClassLoader(),
                new Class<?>[]{Door.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getFacing" -> facing;
                    case "getHinge" -> hinge;
                    case "isPowered" -> powered;
                    case "toString" -> "DoorFake";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException(
                            "Método não esperado no teste: " + method.getName()
                    );
                }
        );
    }
}
