package net.chrotos.rpgapi.criteria;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import net.chrotos.rpgapi.selectors.Player;
import net.chrotos.rpgapi.subjects.QuestSubject;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Getter
@SuperBuilder
public class Inventory extends ItemCriterion {
    /**
     * The count, how many items need to be added to the inventory
     */
    @Builder.Default
    private final Integer count = 1;

    /**
     * The player properties, that need to be fulfilled
     */
    private final Player player;

    @Override
    public boolean check(@NonNull QuestSubject subject, @NonNull ItemStack object) {
        if (!super.check(subject, object)) {
            return false;
        }

        if (player != null && !player.applies(Bukkit.getPlayer(subject.getUniqueId()))) {
            return false;
        }

        return checkIntegerProgress(subject, count, object.getAmount(), false);
    }

    @Override
    public ItemStack getGuiItemStack(Locale locale) {
        if (getGui() != null && getGui().getMaterial() != null) {
            ItemStack itemStack = new ItemStack(getGuiMaterial());

            Component displayName = getGuiDisplayName(locale);

            if (displayName == null) {
                return new ItemStack(Material.AIR);
            }

            ItemMeta meta = itemStack.getItemMeta();

            meta.displayName(displayName);
            meta.lore(getGuiLores(locale));
            itemStack.setItemMeta(meta);

            return itemStack;
        }

        return new ItemStack(Material.AIR);
    }
}
