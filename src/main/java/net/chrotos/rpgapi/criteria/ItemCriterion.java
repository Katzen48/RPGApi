package net.chrotos.rpgapi.criteria;

import lombok.Getter;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import org.bukkit.Material;

import java.util.List;

@Getter
@SuperBuilder
public abstract class ItemCriterion extends Criterion {
    /**
     * The Display Name, of the item. All are substitutes.
     */
    @Singular("displayName")
    private final List<String> displayNames;
    /**
     * The materials, of the item. All are substitutes.
     */
    @Singular("material")
    private final List<Material> materials;
}
