package net.chrotos.rpgapi.manager;

import lombok.*;
import net.chrotos.rpgapi.RPGPlugin;
import net.chrotos.rpgapi.datastorage.config.ConfigStorage;
import net.chrotos.rpgapi.datastorage.playerdata.PlayerStorage;
import net.chrotos.rpgapi.quests.Quest;
import net.chrotos.rpgapi.quests.QuestLine;
import net.chrotos.rpgapi.subjects.DefaultQuestSubject;
import net.chrotos.rpgapi.subjects.QuestProgress;
import net.chrotos.rpgapi.subjects.QuestSubject;
import net.chrotos.rpgapi.utils.CounterLock;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.BiFunction;
import java.util.logging.Logger;

public class QuestManager {
    @NonNull
    private final RPGPlugin plugin;
    private final IdentityHashMap<UUID, QuestSubject> subjectHashMap = new IdentityHashMap<>();
    @NonNull
    private final Logger logger;
    @NonNull
    private final PlayerStorage playerStorage;
    @NonNull
    private final ConfigStorage configStorage;
    //@NonNull
    //private final NPCLoader npcLoader;
    //@Getter
    //private final List<NPC> npcs = Collections.synchronizedList(new ArrayList<>());
    private final List<QuestLine> questLines = new ArrayList<>();
    private final IdentityHashMap<NamespacedKey, Quest> questMap = new IdentityHashMap<>();

    public QuestManager(@NonNull RPGPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.playerStorage = plugin.getPlayerStorage();
        this.configStorage = plugin.getConfigStorage();
        //this.npcLoader = new NPCLoader(plugin);
    }

    @Getter
    @Setter
    @NonNull
    private static BiFunction<UUID, QuestProgress, ? extends QuestSubject> subjectProvider = DefaultQuestSubject::create;

    public Quest getQuest(@NonNull NamespacedKey key) {
        return questMap.get(key);
    }

    @Synchronized
    public QuestSubject getQuestSubject(@NonNull UUID uniqueId) {
        return getQuestSubject(uniqueId, false);
    }

    @Synchronized
    public QuestSubject getQuestSubject(@NonNull UUID uniqueId, boolean load) {
        if (subjectHashMap.containsKey(uniqueId)) {
            return subjectHashMap.get(uniqueId);
        }

        if (load) {
            return loadQuestSubject(uniqueId);
        } else {
            return null;
        }
    }

    @Synchronized
    protected void addQuestSubject(@NonNull QuestSubject questSubject) {
        if (subjectHashMap.containsKey(questSubject.getUniqueId())) {
            return;
        }

        subjectHashMap.put(questSubject.getUniqueId(), questSubject);
    }

    @Synchronized
    protected void removeQuestSubject(QuestSubject questSubject) {
        if (questSubject == null) {
            return;
        }

        subjectHashMap.remove(questSubject.getUniqueId());
        CounterLock.reset(questSubject.getUniqueId());
    }

    @Synchronized
    public QuestSubject loadQuestSubject(@NonNull UUID uniqueId) {
        return subjectProvider.apply(uniqueId, playerStorage.getPlayerData(uniqueId));
    }

    @Synchronized
    public void saveQuestSubject(@NonNull UUID uniqueId) {
        QuestSubject questSubject = getQuestSubject(uniqueId);

        if (questSubject == null) {
            return;
        }

        playerStorage.savePlayerData(questSubject.getUniqueId(), questSubject.getQuestProgress());
    }

    /*
    @Synchronized
    public void loadNPCs() {
        npcs.addAll(npcLoader.getNPCs());

        int npcCount = npcs.size();
        int questCount = npcs.stream().mapToInt(npc -> npc.getQuests().size()).sum();
        logger.info(String.format("%d NPCs with %d quests loaded", npcCount, questCount));
    }

    public NPC getNPC(@NonNull Villager villager) {
        return npcs.stream().filter(npc -> npc.getEntity() == villager).findFirst().orElse(null);
    }
     */

    @Synchronized
    public void loadQuests() {
        logger.info("Loading all Quests");

        List<Quest> quests = configStorage.loadQuests();
        ArrayList<Quest> rootQuests = new ArrayList<>();
        quests.removeIf(quest -> {
            if (quest.getParent() == null) {
                rootQuests.add(quest);
                return true;
            }
            return false;
        });

        rootQuests.forEach(quest -> {
            questLines.add(QuestLine.generate(quest, quests, lineQuest -> questMap.put(lineQuest.getKey(), lineQuest)));
        });
    }

    @Synchronized
    public void onPlayerJoin(@NonNull Player player) {
        try {
            QuestSubject subject = getQuestSubject(player.getUniqueId(), true);
            subject.setPlayer(player);
            addQuestSubject(subject);

            // TODO initialize
            // TODO run initialization actions

        } catch (Throwable throwable) {
            Bukkit.getScheduler().runTask(plugin, () -> player.kick(
                    Component.text("Error whilst initializing Quests!")
                    .color(NamedTextColor.DARK_RED)));

            logger.severe(throwable.toString());
        }
    }

    // TODO re-implement with events
    /*
    @Synchronized
    private boolean checkAlreadyDone(@NonNull QuestSubject subject) {
        Player player = Bukkit.getPlayer(subject.getUniqueId());
        if (player == null) {
            return false;
        }

        checkCompletance(subject, AdvancementDone.class, null);
        checkCompletance(subject, Location.class, player.getLocation());
        checkCompletance(subject, net.chrotos.rpgapi.criteria.Quest.class, null); // TODO fix

        return true;
    }
     */

    @Synchronized
    public void onPlayerQuit(@NonNull Player player) {
        saveQuestSubject(player.getUniqueId());
        removeQuestSubject(getQuestSubject(player.getUniqueId()));
    }

    // TODO re-implement
    /**
    @Synchronized
    public <T> void checkCompletance(@NonNull QuestSubject subject, @NonNull Class<? extends Checkable<T>> clazz, T object) {
        List<Quest> quests = subject.getQuestProgress().getActiveQuests();

        CounterLock.increment(subject.getUniqueId());

        List<Quest> completedQuests = new ArrayList<>();
        List<QuestStep> completedQuestSteps = new ArrayList<>();
        AtomicBoolean questStepsCompleted = new AtomicBoolean();

        quests.removeIf(quest -> {
            final QuestProgress finalQuestProgress = subject.getQuestProgress().stream()
                    .filter(progress -> progress.getQuest() == quest)
                    .findFirst().get();

            boolean removed = finalQuestProgress.getActiveQuestSteps().removeIf(questStep -> {
                questStep.getCriteria().stream().filter(questCriterion -> !finalQuestProgress.getCompletedQuestCriteria()
                                            .contains(questCriterion))
                                        .forEach(questCriterion -> {

                    AtomicInteger criteria = new AtomicInteger();
                    AtomicInteger completed = new AtomicInteger();
                    Arrays.stream(questCriterion.getClass().getDeclaredFields())
                            .filter(field -> Checkable.class.isAssignableFrom(field.getType()))
                            .forEach(field -> {

                        field.setAccessible(true);
                        Checkable<?> fieldValue;
                        try {
                            if ((fieldValue = (Checkable<?>) field.get(questCriterion)) != null) {
                                criteria.incrementAndGet();
                            } else {
                                return;
                            }
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                            return;
                        }

                        if (finalQuestProgress.getCompletedCriteria().contains(fieldValue)) {
                            completed.incrementAndGet();

                            return;
                        }

                        if (!clazz.isInstance(fieldValue)) {
                            return;
                        }

                        Checkable<T> checkable = (Checkable<T>) fieldValue;

                        // If is valid, complete criterion
                        if (checkable.check(subject, object)) {
                            completed.incrementAndGet();
                            finalQuestProgress.getCompletedCriteria().add((Criterion) checkable);
                        }
                    });

                    // If count of completed criteria equals set criteria, complete quest criterion
                    if (completed.get() >= criteria.get()) {
                        finalQuestProgress.getCompletedQuestCriteria().add(questCriterion);
                        finalQuestProgress.getCompletedCriteria().removeIf(criterion -> criterion.getQuestCriterion() == questCriterion);
                    }
                });

                // If no more quest criteria, complete quest step
                if (finalQuestProgress.getCompletedQuestCriteria()
                        .stream().filter(
                                questCriterion -> questCriterion.getQuestStep() == questStep)
                        .count() >= questStep.getCriteria().size()) {

                    finalQuestProgress.getCompletedSteps().add(questStep);
                    finalQuestProgress.getCompletedQuestCriteria().removeIf(
                            questCriterion -> questCriterion.getQuestStep() == questStep);

                    completedQuestSteps.add(questStep);

                    return true;
                }

                return false;
            });

            questStepsCompleted.set(questStepsCompleted.get() || removed);

            // If no more steps, complete quest
            if (quest.getSteps().size() <= finalQuestProgress.getCompletedSteps().size()) {

                subject.getCompletedQuests().add(quest);
                completedQuests.add(quest);
                subject.getQuestProgress().remove(finalQuestProgress);

                // Remove the quest from the active quests
                return true;
            } else if (finalQuestProgress.getActiveQuestSteps().size() < 1) {
                int level = finalQuestProgress.getCompletedSteps().stream()
                        .mapToInt(QuestStep::getLevel).max().getAsInt() + 1;

                quest.getSteps().stream().filter(questStep -> questStep.getLevel() == level)
                        .forEach(finalQuestProgress.getActiveQuestSteps()::add);
            }

            // Quest is still active
            return false;
        });

        if (questStepsCompleted.get()) {
            checkAlreadyDone(subject);
        }

        completedQuestSteps.forEach(subject::complete);
        completedQuests.forEach(quest -> {
            subject.complete(quest);
            checkCompletance(subject, net.chrotos.rpgapi.criteria.Quest.class, quest);
        });

        if (subject.getCompletedQuests().containsAll(subject.getLevel().getQuests())) {
            completeLevel(subject);
        }

        if(CounterLock.decrement(subject.getUniqueId()) < 1) {
            if (questStepsCompleted.get()) {
                saveQuestSubject(subject.getUniqueId());
            }
            CounterLock.reset(subject.getUniqueId());
        }
    }

    private void completeLevel(@NonNull QuestSubject subject) {
        QuestLevel nextLevel = null;
        if (subject.getLevel() == null) {
            if (getQuestGraph().getLevels().size() > 0) {
                nextLevel = getQuestGraph().getLevels().get(0);
            }
        } else {
            nextLevel = subject.getLevel().getNextLevel();
        }

        if (nextLevel == null) {
            return;
        }

        if (subject.getActiveQuests().size() < 1) {
            subject.setLevel(nextLevel);
        }
    }
     **/
}
