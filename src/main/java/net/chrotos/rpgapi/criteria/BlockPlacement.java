package net.chrotos.rpgapi.criteria;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import net.chrotos.rpgapi.subjects.QuestSubject;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.List;

@Getter
@SuperBuilder
public class BlockPlacement extends Criterion implements Checkable<Block> {
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

    public boolean check(@NonNull QuestSubject subject, @NonNull Block block) {
        if (!materials.contains(block.getBlockData().getMaterial())) {
            return false;
        }

        return checkIntegerProgress(subject, count);
    }
}
