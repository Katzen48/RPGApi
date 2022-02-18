package net.chrotos.rpgapi.actions;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import net.chrotos.rpgapi.selectors.IntegerRange;
import org.bukkit.Material;

@Getter
@Builder
public class Loot implements Actionable {
    /**
     * The display name, of the items to be awarded.
     */
    private final String displayName;
    /**
     * The material, of the items to be awarded. Required.
     */
    @NonNull
    private final Material material;
    /**
     * The count, of items to be awarded. If not set, will be one. If both values are assigned, count will be random.
     */
    @NonNull
    private final IntegerRange count;
    /**
     * The durability, of the items to be awarded.
     */
    @Deprecated
    private final Short durability;
}
