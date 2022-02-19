package net.chrotos.rpgapi.subjects;

import net.chrotos.rpgapi.actions.*;
import net.chrotos.rpgapi.quests.Quest;
import net.chrotos.rpgapi.quests.QuestLevel;

import java.util.List;

/**
 * This object may be updated asynchronously.
 * No return value should be cached anywhere and instead be re-fetched, as they are considered volatile.
 */
public interface QuestSubject {
    /**
     * Synchronized method
     * @return the current quest level. If all quests are completed, this is increased.
     */
    QuestLevel getLevel();

    /**
     * Synchronized method
     * @return all quests, that have been completed. Is used for resolution, if the quest level should be increased
     */
    List<Quest> getCompletedQuests();

    /**
     * Synchronized method
     * @return all quests, that are actively been tracked.
     */
    List<Quest> getActiveQuests();

    /**
     * Synchronized method
     * @return progress, of not yet completed quests. Upon quest completion, these values are removed.
     */
    List<QuestProgress> getQuestProgress();

    /**
     * Synchronized method
     * @param advancement the advancement, to be awarded
     */
    void award(Advancement advancement);

    /**
     * Synchronized method
     * @param experience the experience, to be awarded
     */
    void award(Experience experience);

    /**
     * Synchronized method
     * @param loot the loot, to be awarded
     */
    void award(Loot loot);

    /**
     * Synchronized method
     * @param lootTable the loot table, to be awarded
     */
    void award(LootTable lootTable);
    /**
     * Synchronized method
     * @param title the title, to be shown
     */
    void award(Title title);
}
