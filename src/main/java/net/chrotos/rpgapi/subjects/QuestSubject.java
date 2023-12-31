package net.chrotos.rpgapi.subjects;

import lombok.NonNull;
import net.chrotos.rpgapi.actions.*;
import net.chrotos.rpgapi.criteria.Criteria;
import net.chrotos.rpgapi.criteria.CriteriaInstance;
import net.chrotos.rpgapi.manager.QuestManager;
import net.chrotos.rpgapi.quests.Quest;
import net.chrotos.rpgapi.quests.QuestLevel;
import net.chrotos.rpgapi.quests.QuestStep;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;
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

    void setPlayer(@NonNull Player player);

    @NonNull
    Locale getLocale();

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

    void setCompletedQuests(@NonNull List<Quest> completedQuests);
    void setActiveQuests(@NonNull List<Quest> activeQuests);
    QuestProgress getQuestProgress();

    default <T, A extends Criteria<T, A>, C extends CriteriaInstance<T, A>> void trigger(@NonNull C criteriaInstance, @NonNull T value) {
        criteriaInstance.trigger(this, value);
    }

    default <T, A extends Criteria<T, A>, C extends CriteriaInstance<T, A>> void trigger(@NonNull List<C> criteriaInstances, @NonNull T value) {
        criteriaInstances.forEach(criteriaInstance -> trigger(criteriaInstance, value));

        // TODO check completance
    }

    default <T, A extends Criteria<T, A>> void trigger(@NonNull NamespacedKey type, @NonNull Class<A> clazz, @NonNull T value) {
        trigger(getQuestProgress().getCriteriaInstances(type, clazz), value);
    }

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

        Advancement[] advancements = new Advancement[actions.getAdvancements().size()];
        actions.getAdvancements().toArray(advancements);
        award(advancements);

        if (actions.getTitle() != null) {
            award(actions.getTitle());
        }

        actions.getCommands().forEach(this::award);
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
    void activate(@NonNull Quest quest, @NonNull QuestManager questManager);
}
