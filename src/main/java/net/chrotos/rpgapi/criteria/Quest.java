package net.chrotos.rpgapi.criteria;

import lombok.*;
import lombok.experimental.SuperBuilder;
import net.chrotos.rpgapi.subjects.QuestSubject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Getter
@RequiredArgsConstructor
@SuperBuilder
public class Quest extends Criterion implements Checkable<net.chrotos.rpgapi.quests.Quest> {
    /**
     * The id of the quest, that has to be achieved
     */
    @NonNull
    private final String id;
    /**
     * The object instance of the required quest
     */
    @Setter
    private net.chrotos.rpgapi.quests.Quest quest;


    @Override
    public boolean check(@NonNull QuestSubject subject, net.chrotos.rpgapi.quests.Quest object) {
        if (object != null) {
            return object == quest;
        }

        return subject.getCompletedQuests().contains(quest);
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

    @Override
    public List<Component> getGuiLore() {
        return Collections.emptyList();
    }

    @Override
    public TranslatableComponent getGuiName() {
        return null;
    }
}
