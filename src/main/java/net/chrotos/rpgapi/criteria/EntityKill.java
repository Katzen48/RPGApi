package net.chrotos.rpgapi.criteria;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import net.chrotos.rpgapi.subjects.QuestSubject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Getter
@SuperBuilder
public class EntityKill extends EntityCriterion implements Checkable<Entity> {
    /**
     * The count, of entities to be killed (requires type to be set, should not be used with id)
     */
    @Builder.Default
    private final Integer count = 1;

    @Override
    public boolean check(@NonNull QuestSubject subject, @NonNull Entity object) {
        if (!super.check(subject, object)) {
            return false;
        }

        return checkIntegerProgress(subject, count);
    }

    @Override
    public ItemStack getGuiItemStack(Locale locale) {
        ItemStack itemStack = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = itemStack.getItemMeta();

        meta.displayName(orFetch(getGuiDisplayName(locale),
                () -> Component.text(GlobalTranslator.translator().translate(getGuiName().key(), locale).format(null))));
        meta.lore(orFetch(getGuiLores(locale), this::getGuiLore));
        itemStack.setItemMeta(meta);

        return itemStack;
    }

    public List<Component> getGuiLore() {
        List<Component> lore = new ArrayList<>();

        if (getType() != null) {
            lore.add(Component.text("Type: ").append(Component.translatable(getType())));
        }

        if (getDisplayName() != null) {
            lore.add(Component.text("Name: " + getDisplayName()));
        }

        return lore;
    }

    public TranslatableComponent getGuiName() {
        return Component.translatable("quest.criteria.entity.kill");
    }
}
