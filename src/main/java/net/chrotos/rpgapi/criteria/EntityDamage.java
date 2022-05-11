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
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Getter
@SuperBuilder
public class EntityDamage extends EntityCriterion implements Checkable<EntityDamageEvent> {
    /**
     * The damage, to be dealt at the entity. If not set, will be one.
     */
    @Builder.Default
    private final Integer damage = 1;

    @Override
    public boolean check(@NonNull QuestSubject subject, @NonNull EntityDamageEvent object) {
        if (!super.check(subject, object.getEntity())) {
            return false;
        }

        return checkIntegerProgress(subject, damage, (int) object.getFinalDamage());
    }

    @Override
    public ItemStack getGuiItemStack(Locale locale) {
        ItemStack itemStack = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = itemStack.getItemMeta();

        meta.displayName(Component.text(GlobalTranslator.translator()
                .translate(getGuiName().key(), locale).format(null)));
        meta.lore(getGuiLore());
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
        return Component.translatable("quest.criteria.entity.damage");
    }
}
