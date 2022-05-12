package net.chrotos.rpgapi.criteria;

import lombok.NonNull;
import net.chrotos.rpgapi.gui.QuestLogGuiItem;
import net.chrotos.rpgapi.gui.QuestLogItem;
import net.chrotos.rpgapi.quests.QuestCriterion;
import net.chrotos.rpgapi.subjects.QuestSubject;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface Checkable<T> extends QuestLogItem {
    boolean check(@NonNull QuestSubject subject, T object);
}
