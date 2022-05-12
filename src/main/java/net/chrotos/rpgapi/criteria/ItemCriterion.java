package net.chrotos.rpgapi.criteria;

import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import net.chrotos.rpgapi.subjects.QuestSubject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;

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

    @Override
    public ItemStack getGuiItemStack(Locale locale) {
        ItemStack itemStack = new ItemStack(materials.size() == 1 ? materials.get(0) : Material.PLAYER_HEAD, getCount());
        ItemMeta meta = itemStack.getItemMeta();

        meta.displayName(orFetch(getGuiDisplayName(locale),
                () -> Component.text(GlobalTranslator.translator().translate(getGuiName().key(), locale).format(null))));
        meta.lore(orFetch(getGuiLores(locale), this::getGuiLore));
        itemStack.setItemMeta(meta);

        return itemStack;
    }

    public Integer getCount() {
        return 1;
    }

    public List<Component> getGuiLore() {
        List<Component> lore = new ArrayList<>();

        if (materials.size() > 1) {
            Component component = Component.text("Materials: ");

            for (int i = 0; i < materials.size(); i++) {
                if (i > 0) {
                    component = component.append(Component.text(", "));
                }

                component = component.append(Component.translatable(materials.get(i)));
            }

            lore.add(component);
        }

        if (displayNames.size() > 0) {
            Component component = Component.text("Name: ");

            for (int i = 0; i < displayNames.size(); i++) {
                if (i > 0) {
                    component = component.append(Component.text(", "));
                }

                component = component.append(Component.text(displayNames.get(i)));
            }

            lore.add(component);
        }

        return lore;
    }

    public TranslatableComponent getGuiName() {
        return Component.translatable("");
    }
}
