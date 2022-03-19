package net.chrotos.rpgapi.manager;

import com.google.common.collect.Maps;
import lombok.*;
import net.chrotos.rpgapi.config.ConfigStorage;
import net.chrotos.rpgapi.criteria.AdvancementDone;
import net.chrotos.rpgapi.criteria.Checkable;
import net.chrotos.rpgapi.criteria.Criterion;
import net.chrotos.rpgapi.criteria.Location;
import net.chrotos.rpgapi.datastorage.SubjectStorage;
import net.chrotos.rpgapi.npc.NPC;
import net.chrotos.rpgapi.npc.NPCLoader;
import net.chrotos.rpgapi.quests.Quest;
import net.chrotos.rpgapi.quests.QuestGraph;
import net.chrotos.rpgapi.quests.QuestLevel;
import net.chrotos.rpgapi.quests.QuestStep;
import net.chrotos.rpgapi.subjects.DefaultQuestSubject;
import net.chrotos.rpgapi.subjects.QuestProgress;
import net.chrotos.rpgapi.subjects.QuestSubject;
import net.chrotos.rpgapi.utils.CounterLock;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Villager;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class QuestManager {
    private QuestGraph questGraph;
    private final Map<UUID, QuestSubject> subjectHashMap = Maps.newConcurrentMap();
    @NonNull
    private final Logger logger;
    @NonNull
    private final SubjectStorage subjectStorage;
    @NonNull
    private final ConfigStorage configStorage;
    @NonNull
    private final NPCLoader npcLoader;
    @Getter
    private final List<NPC> npcs = Collections.synchronizedList(new ArrayList<>());

    @Getter
    @Setter
    @NonNull
    private static Function<UUID, ? extends QuestSubject> subjectProvider = DefaultQuestSubject::create;

    @Synchronized
    public QuestSubject getQuestSubject(@NonNull UUID uniqueId) {
        if (subjectHashMap.containsKey(uniqueId)) {
            return subjectHashMap.get(uniqueId);
        }

        return loadQuestSubject(uniqueId);
    }

    @Synchronized
    protected void addQuestSubject(@NonNull QuestSubject questSubject) {
        if (subjectHashMap.containsKey(questSubject.getUniqueId())) {
            return;
        }

        subjectHashMap.put(questSubject.getUniqueId(), questSubject);
    }

    @Synchronized
    protected void removeQuestSubject(@NonNull QuestSubject questSubject) {
        subjectHashMap.remove(questSubject.getUniqueId());
        CounterLock.reset(questSubject.getUniqueId());
    }

    @Synchronized
    public QuestSubject loadQuestSubject(@NonNull UUID uniqueId) {
        return subjectStorage.getSubject(uniqueId, questGraph);
    }

    @Synchronized
    public void saveQuestSubject(@NonNull UUID uniqueId) {
        QuestSubject questSubject = getQuestSubject(uniqueId);

        if (questSubject == null) {
            return;
        }

        subjectStorage.saveSubject(questSubject);
    }

    @Synchronized
    public QuestGraph getQuestGraph() {
        if (questGraph == null) {
            logger.info("Generating Quest Graph");
            questGraph = QuestGraph.generate(loadQuests());

            int levels = questGraph.getLevels().size();
            int quests = questGraph.getLevels().stream().mapToInt(level -> level.getQuests().size()).sum();
            int questSteps = questGraph.getLevels().stream().mapToInt(
                            level -> level.getQuests().stream().mapToInt(quest -> quest.getSteps().size()).sum()).sum();
            int questCriteria = questGraph.getLevels().stream().mapToInt(
                    level -> level.getQuests().stream().mapToInt(
                            quest -> quest.getSteps().stream().mapToInt(
                                    step -> step.getCriteria().size()).sum()).sum()).sum();

            logger.info(String.format("Quest Graph contains %d levels with %d quests with %d quest " +
                                            "steps and %d quest criteria", levels, quests, questSteps, questCriteria));
        }

        return questGraph;
    }

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

    @Synchronized
    public Quest loadQuest(@NonNull String id) {
        logger.info("Loading Quest " + id);

        return configStorage.getQuest(id);
    }

    @Synchronized
    @NonNull
    public List<Quest> loadQuests() {
        logger.info("Loading all Quests");

        return configStorage.getQuests();
    }

    @Synchronized
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        try {
            QuestSubject subject = getQuestSubject(event.getPlayer().getUniqueId());
            addQuestSubject(subject);
            boolean initialize = true;
            if (subject.getLevel() == null) {
                completeLevel(subject);
                initialize = false;
            }

            checkAlreadyDone(subject);
            saveQuestSubject(subject.getUniqueId());

            if (initialize) {
                subject.getActiveQuests().stream()
                        .filter(quest -> !quest.getInitializationActions().isOnce())
                        .forEach(quest -> subject.award(quest.getInitializationActions()));
            }
        } catch (Throwable throwable) {
            event.getPlayer().kick(Component.text("Error whilst initializing Quests!")
                    .color(NamedTextColor.DARK_RED));

            throwable.printStackTrace();
        }
    }

    @Synchronized
    private void checkAlreadyDone(@NonNull QuestSubject subject) {
        checkCompletance(subject, AdvancementDone.class, null);
        checkCompletance(subject, Location.class, Bukkit.getPlayer(subject.getUniqueId()).getLocation());
        checkCompletance(subject, net.chrotos.rpgapi.criteria.Quest.class, null);
    }

    @Synchronized
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        saveQuestSubject(event.getPlayer().getUniqueId());
        removeQuestSubject(getQuestSubject(event.getPlayer().getUniqueId()));
    }

    // TODO move to quest class?
    @Synchronized
    public <T> void checkCompletance(@NonNull QuestSubject subject, @NonNull Class<? extends Checkable<T>> clazz, T object) {
        List<Quest> quests = subject.getActiveQuests();

        if (quests == null) {
            return;
        }

        CounterLock.increment(subject.getUniqueId());

        List<Quest> completedQuests = new ArrayList<>();
        AtomicBoolean questStepsCompleted = new AtomicBoolean();

        quests.removeIf(quest -> {
            QuestProgress questProgress = subject.getQuestProgress().stream()
                                                                    .filter(progress -> progress.getQuest() == quest)
                                                                    .findFirst().get();

            final QuestProgress finalQuestProgress = questProgress;
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

                    subject.complete(questStep);

                    return true;
                }

                return false;
            });

            questStepsCompleted.set(questStepsCompleted.get() || removed);

            // If no more steps, complete quest
            if (quest.getSteps().size() <= finalQuestProgress.getCompletedSteps().size()) {

                subject.getCompletedQuests().add(quest);
                subject.complete(quest);
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

        completedQuests.forEach(quest -> {
            checkCompletance(subject, net.chrotos.rpgapi.criteria.Quest.class, quest);
        });

        completeLevel(subject);

        if(CounterLock.decrement(subject.getUniqueId()) < 1) {
            saveQuestSubject(subject.getUniqueId());
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

            // TODO manual activation?
            //subject.getLevel().getQuests().forEach(quest -> subject.activate(quest, this));
        }
    }
}
