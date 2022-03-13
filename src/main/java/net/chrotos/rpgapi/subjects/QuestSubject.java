package net.chrotos.rpgapi.subjects;

import lombok.NonNull;
import lombok.Synchronized;
import net.chrotos.rpgapi.actions.*;
import net.chrotos.rpgapi.quests.Quest;
import net.chrotos.rpgapi.quests.QuestLevel;
import net.chrotos.rpgapi.quests.QuestStep;

import java.util.List;
import java.util.UUID;

/**
 * This object may be updated asynchronously.
 * No return value should be cached anywhere and instead be re-fetched, as they are considered volatile.
 */
public interface QuestSubject {
    /**
     * @return the uniqueId of this subject
     */
    @NonNull
    UUID getUniqueId();

    /**
     * @return the name of the player
     */
    @NonNull
    String getName();

    /**
     * @return the display name of the player
     */
    @Deprecated
    @NonNull
    String getDisplayName();

    void setLevel(@NonNull QuestLevel questLevel);
    void setCompletedQuests(@NonNull List<Quest> completedQuests);
    void setActiveQuests(@NonNull List<Quest> activeQuests);
    void setQuestProgress(@NonNull List<QuestProgress> questProgress);

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
     * @param advancements the advancements, to be awarded
     */
    void award(@NonNull Advancement... advancements);

    /**
     * Synchronized method
     * @param experience the experience, to be awarded
     */
    void award(@NonNull Experience experience);

    /**
     * Synchronized method
     * @param loots the loot, to be awarded
     * @throws IllegalStateException if the inventory is full
     */
    void award(@NonNull Loot... loots) throws IllegalStateException;

    /**
     * Synchronized method
     * @param lootTables the loot tables, to be awarded
     */
    void award(@NonNull LootTable... lootTables);

    /**
     * Synchronized method
     * @param title the title, to be shown
     */
    void award(@NonNull Title title);

    /**
     * Synchronized method
     * @param command the command, to be executed
     */
    void award(@NonNull Command command);

    /**
     * Synchronized method
     * @param actions the actions to be executed
     */
    @Synchronized
    default void award(Actions actions) {
        if (actions == null) {
            return;
        }

        Loot[] loots = new Loot[actions.getLoots().size()];
        actions.getLoots().toArray(loots);
        award(loots);

        LootTable[] lootTables = new LootTable[actions.getLootTables().size()];
        actions.getLootTables().toArray(lootTables);
        award(lootTables);

        if (actions.getExperience() != null) {
            award(actions.getExperience());
        }

        Advancement[] advancements = new Advancement[actions.getLootTables().size()];
        actions.getAdvancements().toArray(advancements);
        award(advancements);

        if (actions.getTitle() != null) {
            award(actions.getTitle());
        }
    }

    /**
     * Synchronized method
     * @param quest the quest to complete
     */
    void complete(@NonNull Quest quest);

    /**
     * Synchronized method
     * @param questStep the quest step to complete
     */
    void complete(@NonNull QuestStep questStep);

    /**
     * Synchronized method
     * @param quest the quest to activate
     */
    void activate(@NonNull Quest quest);
}
