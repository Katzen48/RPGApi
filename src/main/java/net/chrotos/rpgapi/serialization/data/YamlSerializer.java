package net.chrotos.rpgapi.serialization.data;

import lombok.NonNull;
import net.chrotos.rpgapi.criteria.Criterion;
import net.chrotos.rpgapi.datastorage.YamlStore;
import net.chrotos.rpgapi.quests.*;
import net.chrotos.rpgapi.subjects.CriterionProgress;
import net.chrotos.rpgapi.subjects.IntegerCriterionProgress;
import net.chrotos.rpgapi.subjects.QuestProgress;
import net.chrotos.rpgapi.subjects.QuestSubject;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;

public class YamlSerializer implements SubjectSerializer<YamlStore> {
    private YamlStore subjectStorage;
    private Function<UUID, ? extends QuestSubject> subjectFunction;

    @Override
    public void initialize(@NotNull YamlStore subjectStorage, @NonNull Function<UUID, ? extends QuestSubject> subjectFunction) {
        this.subjectStorage = subjectStorage;
        this.subjectFunction = subjectFunction;
    }

    @Override
    public QuestSubject getSubject(@NonNull UUID uniqueId, @NonNull QuestGraph questGraph) {
        FileConfiguration config = subjectStorage.getRaw(uniqueId.toString(), true);
        QuestSubject subject = subjectFunction.apply(uniqueId);

        if (!config.contains("level") || !config.contains("completed") || !config.contains("active")
                || !config.contains("progress")) {
            return subject;
        }

        QuestLevel level = questGraph.getQuestLevel(config.getInt("level"));
        subject.setLevel(level);

        List<Quest> completedQuests = Collections.synchronizedList(new ArrayList<>());
        for (String questId : config.getStringList("completed")) {
            Quest quest = questGraph.getQuest(questId);

            if (quest != null) {
                completedQuests.add(quest);
            }
        }
        subject.setCompletedQuests(completedQuests);

        List<Quest> activeQuests = Collections.synchronizedList(new ArrayList<>());
        for (String questId : config.getStringList("active")) {
            Quest quest = questGraph.getQuest(questId);

            if (quest != null) {
                activeQuests.add(quest);
            }
        }
        subject.setActiveQuests(activeQuests);

        List<QuestProgress> questProgress = Collections.synchronizedList(new ArrayList<>());
        for (Map<?, ?> progress : config.getMapList("progress")) {
            QuestProgress progressInstance = mapQuestProgress(progress, questGraph);

            if (progressInstance != null) {
                questProgress.add(progressInstance);
            }
        }
        subject.setQuestProgress(questProgress);

        return subject;
    }

    private QuestProgress mapQuestProgress(@NonNull Map<?, ?> questProgress, @NonNull QuestGraph questGraph) {
        Quest quest = questGraph.getQuest((String) questProgress.get("quest"));
        assert quest != null;

        List<QuestStep> completedSteps = Collections.synchronizedList(new ArrayList<>());
        if (questProgress.containsKey("completedSteps")) {
            for (int step : (List<Integer>) questProgress.get("completedSteps")) {
                completedSteps.add(mapQuestStep(step, quest));
            }
        }

        List<QuestCriterion> completedQuestCriteria = Collections.synchronizedList(new ArrayList<>());
        if (questProgress.containsKey("completedQuestCriteria")) {
            List<Map<?, ?>> questCriteria = (List<Map<?, ?>>) questProgress.get("completedQuestCriteria");
            for (int i = 0; i < questCriteria.size(); i++) {
                completedQuestCriteria.add(mapQuestCriterion(questCriteria.get(i), quest));
            }
        }

        List<Criterion> completedCriteria = Collections.synchronizedList(new ArrayList<>());
        if (questProgress.containsKey("completedCriteria")) {
            List<Map<?, ?>> criteria = (List<Map<?, ?>>) questProgress.get("completedCriteria");
            for (int i = 0; i < criteria.size(); i++) {
                completedCriteria.add(mapCriterion(criteria.get(i), quest));
            }
        }

        List<CriterionProgress<? extends Criterion>> criterionProgresses = Collections.synchronizedList(new ArrayList<>());
        if (questProgress.containsKey("criterionProgress")) {
            List<Map<?, ?>> criterionProgress = (List<Map<?, ?>>) questProgress.get("criterionProgress");
            for (int i = 0; i < criterionProgress.size(); i++) {
                criterionProgresses.add(mapCriterionProgress(criterionProgress.get(i), quest));
            }
        }

        return new QuestProgress(quest, completedSteps, completedQuestCriteria, completedCriteria, criterionProgresses);
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

    }
}
