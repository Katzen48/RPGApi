package net.chrotos.rpgapi.manager;

import com.google.common.collect.Maps;
import lombok.*;
import net.chrotos.rpgapi.config.ConfigStorage;
import net.chrotos.rpgapi.criteria.Checkable;
import net.chrotos.rpgapi.criteria.Criterion;
import net.chrotos.rpgapi.criteria.eventhandler.InventoryChangeEventHandler;
import net.chrotos.rpgapi.datastorage.SubjectStorage;
import net.chrotos.rpgapi.quests.Quest;
import net.chrotos.rpgapi.quests.QuestGraph;
import net.chrotos.rpgapi.subjects.QuestProgress;
import net.chrotos.rpgapi.subjects.QuestSubject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
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
    @Getter
    @Setter
    private static Function<UUID, ? extends QuestSubject> subjectProvider;

    @Synchronized
    public QuestSubject getQuestSubject(UUID uniqueId) {
        return getQuestSubject(uniqueId, false);
    }

    @Synchronized
    public QuestSubject getQuestSubject(UUID uniqueId, boolean elseCreate) {
        if (subjectHashMap.containsKey(uniqueId)) {
            return subjectHashMap.get(uniqueId);
        } else {
            QuestSubject subject = loadQuestSubject(uniqueId);

            if (subject != null || !elseCreate) {
                return subject;
            }

            return subjectProvider.apply(uniqueId);
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
    protected void removeQuestSubject(@NonNull QuestSubject questSubject) {
        if (!subjectHashMap.containsKey(questSubject.getUniqueId())) {
            return;
        }

        subjectHashMap.remove(questSubject.getUniqueId());
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

            subject.getActiveQuests().stream()
                                    .filter(quest -> !quest.getInitializationActions().isOnce())
                                    .forEach(quest -> subject.award(quest.getInitializationActions()));

            InventoryChangeEventHandler.InventoryInvocationHandler.inject(event.getPlayer());
        } catch (Throwable throwable) {
            event.getPlayer().kick(Component.text("Error whilst initializing Quests!")
                    .color(NamedTextColor.DARK_RED));

            throwable.printStackTrace();
        }
    }

    @Synchronized
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        saveQuestSubject(event.getPlayer().getUniqueId());
        removeQuestSubject(getQuestSubject(event.getPlayer().getUniqueId()));
    }

    // TODO move to quest class?
    @Synchronized
    public <T> void checkCompletance(@NonNull QuestSubject subject, @NonNull Class<? extends Checkable<T>> clazz, T object) {
        subject.getActiveQuests().removeIf(quest -> {
            QuestProgress questProgress = subject.getQuestProgress().stream()
                                                                    .filter(progress -> progress.getQuest() == quest)
                                                                    .findFirst().orElse(null);

            if (questProgress == null) {
                questProgress = QuestProgress.builder().quest(quest).build();
                subject.getQuestProgress().add(questProgress);
            }

            final QuestProgress finalQuestProgress = questProgress;
            quest.getSteps().stream().filter(questStep -> !finalQuestProgress.getCompletedSteps().contains(questStep))
                                        .forEach(questStep -> {

                questStep.getCriteria().stream().filter(questCriterion -> !finalQuestProgress.getCompletedQuestCriteria()
                                            .contains(questCriterion))
                                        .forEach(questCriterion -> {


                    AtomicInteger criteria = new AtomicInteger();
                    AtomicInteger completed = new AtomicInteger();
                    Arrays.stream(questCriterion.getClass().getFields())
                            .filter(field -> Criterion.class.isAssignableFrom(field.getDeclaringClass()))
                            .forEach(field -> {

                        field.setAccessible(true);
                        Criterion fieldValue;
                        try {
                            if ((fieldValue = (Criterion) field.get(questCriterion)) != null) {
                                criteria.incrementAndGet();
                            } else {
                                return;
                            }
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                            return;
                        }

                        if (!(clazz.isAssignableFrom(fieldValue.getClass()))) {
                            if (finalQuestProgress.getCompletedCriteria().contains(fieldValue)) {
                                completed.incrementAndGet();
                            }

                            return;
                        }

                        Checkable<T> checkable = (Checkable<T>) fieldValue;

                        // If is valid, complete criterion
                        if (checkable.check(subject, object)) {
                            finalQuestProgress.getCompletedCriteria().add((Criterion) checkable);
                            completed.getAndIncrement();
                        }
                    });

                    // If count of completed criteria equals set criteria, complete quest criterion
                    if (completed.get() >= criteria.get()) {
                        finalQuestProgress.getCompletedQuestCriteria().add(questCriterion);
                    }
                });

                // If no more quest criteria, complete quest step
                if (finalQuestProgress.getCompletedQuestCriteria().size() >= questStep.getCriteria().size()) {
                    finalQuestProgress.getCompletedSteps().add(questStep);
                    subject.complete(questStep);
                }
            });

            // If no more required steps, complete quest
            if (quest.getSteps().stream()
                    .noneMatch(questStep -> questStep.isRequired() && !finalQuestProgress.getCompletedSteps()
                            .contains(questStep))) {

                subject.getCompletedQuests().add(quest);
                subject.complete(quest);
                checkCompletance(subject, net.chrotos.rpgapi.criteria.Quest.class, quest);

                // Remove the quest from the active quests
                return true;
            }

            // Quest is still active
            return false;
        });

        // Activate next level
        if (subject.getActiveQuests().size() < 1) {
            subject.setLevel(subject.getLevel().getNextLevel());

            // TODO manual activation?
            if (subject.getLevel() != null) {
                subject.getLevel().getQuests().forEach(quest -> subject.activate(quest, this));
            }
        }
    }
}
