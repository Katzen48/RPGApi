package net.chrotos.rpgapi.criteria;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.chrotos.rpgapi.subjects.QuestSubject;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

@Getter
@RequiredArgsConstructor
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
        return new ItemStack(Material.AIR);
    }
}
