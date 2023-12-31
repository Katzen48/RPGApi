package net.chrotos.rpgapi.criteria;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import net.chrotos.rpgapi.criteria.instances.IntegerInstance;
import net.chrotos.rpgapi.subjects.QuestSubject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@Getter
@Builder
public abstract class ItemCriterion<T, A extends ItemCriterion<T, A>> extends SimpleCriteria<T, A> {

    @Singular("displayName")
    private final List<String> displayNames;

    @Singular("material")
    private final List<Material> materials;

    private final int customModelData;

    private final int count;

    public boolean check(@NonNull QuestSubject subject, @NonNull T value, @NonNull IntegerInstance<T, A> instance) {
        ItemStack object = getItemStack(value);

        return (displayNames.isEmpty() || (object.hasItemMeta() && object.getItemMeta().displayName() != null &&
                        displayNames.contains(getComponentAsPlain(object.getItemMeta().displayName())))) &&
                (materials.isEmpty() || materials.contains(object.getType())) &&
                (customModelData == 0 || (object.hasItemMeta() && object.getItemMeta().hasCustomModelData() &&
                        object.getItemMeta().getCustomModelData() == customModelData));
    }

    @Override
    public void trigger(@NonNull QuestSubject subject, @NonNull T value, @NonNull CriteriaInstance<T, A> instance) {
        if (instance instanceof IntegerInstance<T, A> integerInstance) {
            if (check(subject, value, integerInstance)) {
                this.completed = true;
            }
        }
    }

    @NonNull
    abstract ItemStack getItemStack(@NonNull T value);

    protected String getComponentAsPlain(@NonNull Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }
}
