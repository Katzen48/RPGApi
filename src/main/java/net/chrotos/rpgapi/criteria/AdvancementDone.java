package net.chrotos.rpgapi.criteria;

import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import net.chrotos.rpgapi.subjects.QuestSubject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Getter
@SuperBuilder
public class AdvancementDone extends Criterion implements Checkable<Advancement> {
    /**
     * The keys, of the advancements to be done. All are substitutes.
     */
    @Singular("key")
    private final List<NamespacedKey> keys;

    @Override
    public boolean check(@NonNull QuestSubject subject, Advancement advancement) {
        if (advancement != null) {
            return keys.contains(advancement.getKey());
        }

        return keys.stream().anyMatch(
                key -> Bukkit.getPlayer(subject.getUniqueId()).getAdvancementProgress(Bukkit.getAdvancement(key)).isDone());
    }

    @Override
    public ItemStack getGuiItemStack(Locale locale) {
        ItemStack itemStack = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta meta = itemStack.getItemMeta();

        meta.displayName(orFetch(getGuiDisplayName(locale),
                () -> Component.text(GlobalTranslator.translator().translate(getGuiName().key(), locale).format(null))));
        meta.lore(orFetch(getGuiLores(locale), this::getGuiLore));
        itemStack.setItemMeta(meta);

        return itemStack;
    }

    public List<Component> getGuiLore() {
        return keys.stream().map(advancement -> Bukkit.getAdvancement(advancement).getDisplay().title())
                .collect(Collectors.toList());
    }

    public TranslatableComponent getGuiName() {
        return Component.translatable("quest.criteria.advancement");
    }
}
