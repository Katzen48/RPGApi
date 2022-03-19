package net.chrotos.rpgapi.serialization.data;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.chrotos.rpgapi.criteria.Criterion;
import net.chrotos.rpgapi.datastorage.YamlStore;
import net.chrotos.rpgapi.manager.QuestManager;
import net.chrotos.rpgapi.quests.*;
import net.chrotos.rpgapi.subjects.CriterionProgress;
import net.chrotos.rpgapi.subjects.IntegerCriterionProgress;
import net.chrotos.rpgapi.subjects.QuestProgress;
import net.chrotos.rpgapi.subjects.QuestSubject;
import org.bukkit.configuration.file.FileConfiguration;

import java.lang.reflect.Field;
import java.util.*;

@RequiredArgsConstructor
public class YamlSerializer implements SubjectSerializer<YamlStore> {
    @NonNull
    private final YamlStore subjectStorage;

    @Override
    public QuestSubject getSubject(@NonNull UUID uniqueId, @NonNull QuestGraph questGraph) {
        FileConfiguration config = subjectStorage.getRaw(uniqueId.toString(), true);
        QuestSubject subject = QuestManager.getSubjectProvider().apply(uniqueId);

        subject.setQuestProgress(Collections.synchronizedList(new ArrayList<>()));
        subject.setActiveQuests(Collections.synchronizedList(new ArrayList<>()));
        subject.setCompletedQuests(Collections.synchronizedList(new ArrayList<>()));

        if (!config.contains("level") || !config.contains("completed") || !config.contains("active")
                || !config.contains("progress")) {

            return subject;
        }

        QuestLevel level = questGraph.getQuestLevel(config.getInt("level"));
        subject.setLevel(level);

        for (String questId : config.getStringList("completed")) {
            Quest quest = questGraph.getQuest(questId);

            if (quest != null) {
                subject.getCompletedQuests().add(quest);
            }
        }

        for (String questId : config.getStringList("active")) {
            Quest quest = questGraph.getQuest(questId);

            if (quest != null) {
                subject.getActiveQuests().add(quest);
            }
        }

        for (Map<?, ?> progress : config.getMapList("progress")) {
            QuestProgress progressInstance = mapQuestProgress(progress, questGraph);
            subject.getQuestProgress().add(progressInstance);
        }

        return subject;
    }

    private QuestProgress mapQuestProgress(@NonNull Map<?, ?> questProgress, @NonNull QuestGraph questGraph) {
        Quest quest = questGraph.getQuest((String) questProgress.get("quest"));
        assert quest != null;

        QuestProgress.QuestProgressBuilder builder = QuestProgress.builder();
        builder.quest(quest);

        List<QuestStep> activeSteps = Collections.synchronizedList(new ArrayList<>());
        if (questProgress.containsKey("activeSteps")) {
            for (int step : (List<Integer>) questProgress.get("activeSteps")) {
                activeSteps.add(mapQuestStep(step, quest));
            }
        }
        builder.activeQuestSteps(activeSteps);

        List<QuestStep> completedSteps = Collections.synchronizedList(new ArrayList<>());
        if (questProgress.containsKey("completedSteps")) {
            for (int step : (List<Integer>) questProgress.get("completedSteps")) {
                completedSteps.add(mapQuestStep(step, quest));
            }
        }
        builder.completedSteps(completedSteps);

        List<QuestCriterion> completedQuestCriteria = Collections.synchronizedList(new ArrayList<>());
        if (questProgress.containsKey("completedQuestCriteria")) {
            List<Map<?, ?>> questCriteria = (List<Map<?, ?>>) questProgress.get("completedQuestCriteria");
            for (int i = 0; i < questCriteria.size(); i++) {
                completedQuestCriteria.add(mapQuestCriterion(questCriteria.get(i), quest));
            }
        }
        builder.completedQuestCriteria(completedQuestCriteria);

        List<Criterion> completedCriteria = Collections.synchronizedList(new ArrayList<>());
        if (questProgress.containsKey("completedCriteria")) {
            List<Map<?, ?>> criteria = (List<Map<?, ?>>) questProgress.get("completedCriteria");
            for (int i = 0; i < criteria.size(); i++) {
                completedCriteria.add(mapCriterion(criteria.get(i), quest));
            }
        }
        builder.completedCriteria(completedCriteria);

        List<CriterionProgress<? extends Criterion>> criterionProgresses = Collections.synchronizedList(new ArrayList<>());
        if (questProgress.containsKey("criterionProgress")) {
            List<Map<?, ?>> criterionProgress = (List<Map<?, ?>>) questProgress.get("criterionProgress");
            for (int i = 0; i < criterionProgress.size(); i++) {
                criterionProgresses.add(mapCriterionProgress(criterionProgress.get(i), quest));
            }
        }
        builder.criterionProgresses(criterionProgresses);

        return builder.build();
    }

    private CriterionProgress<? extends Criterion> mapCriterionProgress(@NonNull Map<?,?> criterionProgress,
                                                                        @NonNull Quest quest) {
        Criterion criterion = mapCriterion(criterionProgress, quest);
        if (criterionProgress.containsKey("integer")) {
            return new IntegerCriterionProgress(criterion, (int) criterionProgress.get("integer"));
        }

        return null;
    }

    private Criterion mapCriterion(@NonNull Map<?,?> criterion, @NonNull Quest quest) {
        QuestCriterion questCriterion = mapQuestCriterion(criterion, quest);
        String type = (String) criterion.get("type");

        try {
            Field field = questCriterion.getClass().getDeclaredField(type);
            field.setAccessible(true);
            Object object = field.get(questCriterion);

             if (! (object instanceof Criterion)) {
                 throw new IllegalStateException(type + " is not a criterion");
             }

             return (Criterion) object;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    private QuestCriterion mapQuestCriterion(@NonNull Map<?, ?> questCriterion, @NonNull Quest quest) {
        QuestStep step = mapQuestStep((int) questCriterion.get("questStep"), quest);

        return step.getCriteria().get((int) questCriterion.get("questCriterion"));

    }

    private QuestStep mapQuestStep(int questStep, @NonNull Quest quest) {
        return quest.getSteps().get(questStep);
    }

    @Override
    public void saveSubject(@NonNull QuestSubject questSubject) {
        FileConfiguration config = subjectStorage.getRaw(questSubject.getUniqueId().toString(), true);

        QuestLevel level = questSubject.getLevel();
        config.set("level", level != null ? level.getLevel() : null);

        List<String> completed = new ArrayList<>();
        questSubject.getCompletedQuests().forEach(quest -> completed.add(quest.getId()));
        config.set("completed", completed);

        List<String> active = new ArrayList<>();
        questSubject.getActiveQuests().forEach(quest -> active.add(quest.getId()));
        config.set("active", active);

        // Quest Progress
        List<Map<?, ?>> progress = new ArrayList<>();
        questSubject.getQuestProgress().forEach(questProgress -> {
            Map<String, Object> map = new HashMap<>();

            map.put("quest", questProgress.getQuest().getId());

            // Active Steps
            List<Integer> activeSteps = new ArrayList<>();
            questProgress.getActiveQuestSteps().forEach(step -> {
                activeSteps.add(questProgress.getQuest().getSteps().indexOf(step));
            });
            map.put("activeSteps", activeSteps);

            // Completed Steps
            List<Integer> completedSteps = new ArrayList<>();
            questProgress.getCompletedSteps().forEach(step -> {
                completedSteps.add(questProgress.getQuest().getSteps().indexOf(step));
            });
            map.put("completedSteps", completedSteps);

            // Completed Quest Criteria
            List<Map<?, ?>> completedQuestCriteria = new ArrayList<>();
            questProgress.getCompletedQuestCriteria().forEach(criterion -> {
                Map<String, Object> criterionMap = new HashMap<>();

                criterionMap.put("questStep", criterion.getQuestStep().getQuest()
                                                .getSteps().indexOf(criterion.getQuestStep()));

                criterionMap.put("questCriterion", criterion.getQuestStep().getCriteria().indexOf(criterion));

                completedQuestCriteria.add(criterionMap);
            });
            map.put("completedQuestCriteria", completedQuestCriteria);

            // Completed Criteria
            List<Map<?, ?>> completedCriteria = new ArrayList<>();
            questProgress.getCompletedCriteria().forEach(criterion -> {
                Map<String, Object> criterionMap = new HashMap<>();

                criterionMap.put("questStep", criterion.getQuestCriterion().getQuestStep().getQuest()
                        .getSteps().indexOf(criterion.getQuestCriterion().getQuestStep()));

                criterionMap.put("questCriterion", criterion.getQuestCriterion().getQuestStep().getCriteria()
                        .indexOf(criterion.getQuestCriterion()));

                criterionMap.put("type", Arrays.stream(criterion.getQuestCriterion().getClass().getDeclaredFields())
                        .filter(field -> field.getType().isAssignableFrom(criterion.getClass()))
                        .findFirst().get().getName());

                completedCriteria.add(criterionMap);
            });
            map.put("completedCriteria", completedCriteria);

            // Criterion Progress
            List<Map<?, ?>> criterionProgresses = new ArrayList<>();
            questProgress.getCriterionProgresses().forEach(criterionProgress -> {
                Map<String, Object> progressMap = new HashMap<>();

                progressMap.put("questStep", criterionProgress.getCriterion().getQuestCriterion().getQuestStep()
                        .getQuest().getSteps().indexOf(
                                criterionProgress.getCriterion().getQuestCriterion().getQuestStep()));

                progressMap.put("questCriterion", criterionProgress.getCriterion().getQuestCriterion()
                            .getQuestStep().getCriteria()
                        .indexOf(criterionProgress.getCriterion().getQuestCriterion()));

                progressMap.put("type", Arrays.stream(criterionProgress.getCriterion().getQuestCriterion().getClass()
                        .getDeclaredFields()).filter(field -> field.getType()
                            .isAssignableFrom(criterionProgress.getCriterion().getClass()))
                        .findFirst().get().getName());

                if (criterionProgress instanceof IntegerCriterionProgress) {
                    progressMap.put("integer", ((IntegerCriterionProgress)criterionProgress).getValue());
                } else {
                    throw new IllegalStateException(criterionProgress.getClass().getName() + " is not serializable");
                }

                criterionProgresses.add(progressMap);
            });
            map.put("criterionProgress", criterionProgresses);

            progress.add(map);
        });
        config.set("progress", progress);

        subjectStorage.save(config, questSubject.getUniqueId().toString());
    }
}
