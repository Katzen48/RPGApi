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
    @Singular("material")
    private final List<Material> materials;
    /**
     * The count, of blocks to be placed. If not set, min will be 1
     */
    @Builder.Default
    private final Integer count = 1;
}
