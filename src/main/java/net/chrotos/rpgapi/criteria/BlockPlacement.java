package net.chrotos.rpgapi.criteria;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import org.bukkit.Material;

import java.util.List;

@Getter
@SuperBuilder
public class BlockPlacement extends Criterion {
    /**
     * The materials, of the block to be placed. All are substitutes.
     */
    @Singular("blockMaterial")
    private final List<Material> blockMaterials;
    /**
     * The items, of the block to be placed. All are substitutes.
     */
    @Singular("item")
    private final List<ItemCriterion> items;
    /**
     * The count, of blocks to be placed. If not set, min will be 1
     */
    private final Integer count = 1;
}
