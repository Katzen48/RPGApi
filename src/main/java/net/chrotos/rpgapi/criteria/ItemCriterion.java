package net.chrotos.rpgapi.criteria;

import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import net.chrotos.rpgapi.subjects.QuestSubject;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@Getter
@SuperBuilder
public abstract class ItemCriterion extends Criterion implements Checkable<ItemStack> {
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

    @Override
    public boolean check(@NonNull QuestSubject subject, ItemStack object) {
        return (displayNames.isEmpty() || (object.hasItemMeta() && object.getItemMeta().displayName() != null &&
                        displayNames.contains(getComponentAsPlain(object.getItemMeta().displayName())))) &&
                (materials.isEmpty() || materials.contains(object.getType()));
    }
}
