package net.chrotos.rpgapi.serialization.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.chrotos.rpgapi.criteria.Criterion;
import net.chrotos.rpgapi.datastorage.ChrotosCloudStore;
import net.chrotos.rpgapi.manager.QuestManager;
import net.chrotos.rpgapi.quests.*;
import net.chrotos.rpgapi.subjects.CriterionProgress;
import net.chrotos.rpgapi.subjects.IntegerCriterionProgress;
import net.chrotos.rpgapi.subjects.QuestProgress;
import net.chrotos.rpgapi.subjects.QuestSubject;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@RequiredArgsConstructor
public class ChrotosCloudSerializer implements SubjectSerializer<ChrotosCloudStore> {
    @NonNull
    private final ChrotosCloudStore store;

    @Override
    public QuestSubject getSubject(@NonNull UUID uniqueId, @NonNull QuestGraph questGraph) {
        JsonObject object = store.getRaw(uniqueId);
        QuestSubject subject = QuestManager.getSubjectProvider().apply(uniqueId);

        subject.setQuestProgress(Collections.synchronizedList(new CopyOnWriteArrayList<>()));
        subject.setActiveQuests(Collections.synchronizedList(new CopyOnWriteArrayList<>()));
        subject.setCompletedQuests(Collections.synchronizedList(new CopyOnWriteArrayList<>()));

        if (!object.has("level") || !object.has("completed") ||
                !object.has("active") || !object.has("progress")) {

            return subject;
        }

        QuestLevel level = questGraph.getQuestLevel(object.get("level").getAsInt());
        subject.setLevel(level);

        for (JsonElement completed : object.getAsJsonArray("completed")) {
            Quest quest = questGraph.getQuest(completed.getAsString());

            if (quest != null) {
                subject.getCompletedQuests().add(quest);
            }
        }

        for (JsonElement active : object.getAsJsonArray("active")) {
            Quest quest = questGraph.getQuest(active.getAsString());

            if (quest != null) {
                subject.getActiveQuests().add(quest);
            }
        }

        for (JsonElement progress : object.getAsJsonArray("progress")) {
            QuestProgress progressInstance = mapQuestProgress(progress.getAsJsonObject(), questGraph);
            subject.getQuestProgress().add(progressInstance);
        }

        return subject;
    }

    private QuestProgress mapQuestProgress(@NonNull JsonObject questProgress, @NonNull QuestGraph questGraph) {
        Quest quest = questGraph.getQuest(questProgress.get("quest").getAsString());
        assert quest != null;

        QuestProgress.QuestProgressBuilder builder = QuestProgress.builder();
        builder.quest(quest);

        List<QuestStep> activeSteps = Collections.synchronizedList(new CopyOnWriteArrayList<>());
        if (questProgress.has("activeSteps")) {
            for (JsonElement step : questProgress.get("activeSteps").getAsJsonArray()) {
                activeSteps.add(mapQuestStep(step.getAsInt(), quest));
            }
        }
        builder.activeQuestSteps(activeSteps);

        List<QuestStep> completedSteps = Collections.synchronizedList(new CopyOnWriteArrayList<>());
        if (questProgress.has("completedSteps")) {
            for (JsonElement step : questProgress.get("completedSteps").getAsJsonArray()) {
                completedSteps.add(mapQuestStep(step.getAsInt(), quest));
            }
        }
        builder.completedSteps(completedSteps);

        List<QuestCriterion> completedQuestCriteria = Collections.synchronizedList(new CopyOnWriteArrayList<>());
        if (questProgress.has("completedQuestCriteria")) {
            for (JsonElement questCriterion : questProgress.get("completedQuestCriteria").getAsJsonArray()) {
                completedQuestCriteria.add(mapQuestCriterion(questCriterion.getAsJsonObject(), quest));
            }
        }
        builder.completedQuestCriteria(completedQuestCriteria);

        List<Criterion> completedCriteria = Collections.synchronizedList(new CopyOnWriteArrayList<>());
        if (questProgress.has("completedCriteria")) {
            for (JsonElement criterion : questProgress.get("completedCriteria").getAsJsonArray()) {
                completedCriteria.add(mapCriterion(criterion.getAsJsonObject(), quest));
            }
        }
        builder.completedCriteria(completedCriteria);

        List<CriterionProgress<? extends Criterion>> criterionProgresses = Collections.synchronizedList(new CopyOnWriteArrayList<>());
        if (questProgress.has("criterionProgress")) {
            for (JsonElement criterionProgress : questProgress.get("criterionProgress").getAsJsonArray()) {
                criterionProgresses.add(mapCriterionProgress(criterionProgress.getAsJsonObject(), quest));
            }
        }
        builder.criterionProgresses(criterionProgresses);

        return builder.build();
    }

    private CriterionProgress<? extends Criterion> mapCriterionProgress(@NonNull JsonObject criterionProgress,
                                                                        @NonNull Quest quest) {
        Criterion criterion = mapCriterion(criterionProgress, quest);
        if (criterionProgress.has("integer")) {
            return new IntegerCriterionProgress(criterion, criterionProgress.get("integer").getAsInt());
        }

        return null;
    }

    private Criterion mapCriterion(@NonNull JsonObject criterion, @NonNull Quest quest) {
        QuestCriterion questCriterion = mapQuestCriterion(criterion, quest);
        String type = criterion.get("type").getAsString();

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

    private QuestCriterion mapQuestCriterion(@NonNull JsonObject questCriterion, @NonNull Quest quest) {
        QuestStep step = mapQuestStep(questCriterion.get("questStep").getAsInt(), quest);

        return step.getCriteria().get(questCriterion.get("questCriterion").getAsInt());
    }

    private QuestStep mapQuestStep(int questStep, @NonNull Quest quest) {
        return quest.getSteps().get(questStep);
    }

    @Override
    public void saveSubject(@NonNull QuestSubject questSubject) {
        JsonObject object = new JsonObject();

        QuestLevel level = questSubject.getLevel();
        if (level != null) {
            object.addProperty("level", level.getLevel());
        }

        JsonArray completed = new JsonArray();
        questSubject.getCompletedQuests().forEach(quest -> completed.add(quest.getId()));
        object.add("completed", completed);

        JsonArray active = new JsonArray();
        questSubject.getActiveQuests().forEach(quest -> active.add(quest.getId()));
        object.add("active", active);

        // Quest Progress
        JsonArray progress = new JsonArray();
        questSubject.getQuestProgress().forEach(questProgress -> {
            JsonObject map = new JsonObject();

            map.addProperty("quest", questProgress.getQuest().getId());

            // Active Steps
            JsonArray activeSteps = new JsonArray();
            questProgress.getActiveQuestSteps().forEach(step -> {
                activeSteps.add(questProgress.getQuest().getSteps().indexOf(step));
            });
            map.add("activeSteps", activeSteps);

            // Completed Steps
            JsonArray completedSteps = new JsonArray();
            questProgress.getCompletedSteps().forEach(step -> {
                completedSteps.add(questProgress.getQuest().getSteps().indexOf(step));
            });
            map.add("completedSteps", completedSteps);

            // Completed Quest Criteria
            JsonArray completedQuestCriteria = new JsonArray();
            questProgress.getCompletedQuestCriteria().forEach(criterion -> {
                JsonObject criterionMap = new JsonObject();

                criterionMap.addProperty("questStep", criterion.getQuestStep().getQuest()
                        .getSteps().indexOf(criterion.getQuestStep()));

                criterionMap.addProperty("questCriterion", criterion.getQuestStep().getCriteria().indexOf(criterion));

                completedQuestCriteria.add(criterionMap);
            });
            map.add("completedQuestCriteria", completedQuestCriteria);

            // Completed Criteria
            JsonArray completedCriteria = new JsonArray();
            questProgress.getCompletedCriteria().forEach(criterion -> {
                JsonObject criterionMap = new JsonObject();

                criterionMap.addProperty("questStep", criterion.getQuestCriterion().getQuestStep().getQuest()
                        .getSteps().indexOf(criterion.getQuestCriterion().getQuestStep()));

                criterionMap.addProperty("questCriterion", criterion.getQuestCriterion().getQuestStep().getCriteria()
                        .indexOf(criterion.getQuestCriterion()));

                criterionMap.addProperty("type", Arrays.stream(criterion.getQuestCriterion().getClass().getDeclaredFields())
                        .filter(field -> field.getType().isAssignableFrom(criterion.getClass()))
                        .findFirst().get().getName());

                completedCriteria.add(criterionMap);
            });
            map.add("completedCriteria", completedCriteria);

            // Criterion Progress
            JsonArray criterionProgresses = new JsonArray();
            questProgress.getCriterionProgresses().forEach(criterionProgress -> {
                JsonObject progressMap = new JsonObject();

                progressMap.addProperty("questStep", criterionProgress.getCriterion().getQuestCriterion().getQuestStep()
                        .getQuest().getSteps().indexOf(
                                criterionProgress.getCriterion().getQuestCriterion().getQuestStep()));

                progressMap.addProperty("questCriterion", criterionProgress.getCriterion().getQuestCriterion()
                        .getQuestStep().getCriteria()
                        .indexOf(criterionProgress.getCriterion().getQuestCriterion()));

                progressMap.addProperty("type", Arrays.stream(criterionProgress.getCriterion().getQuestCriterion().getClass()
                        .getDeclaredFields()).filter(field -> field.getType()
                        .isAssignableFrom(criterionProgress.getCriterion().getClass()))
                        .findFirst().get().getName());

                if (criterionProgress instanceof IntegerCriterionProgress) {
                    progressMap.addProperty("integer", ((IntegerCriterionProgress)criterionProgress).getValue());
                } else {
                    throw new IllegalStateException(criterionProgress.getClass().getName() + " is not serializable");
                }

                criterionProgresses.add(progressMap);
            });
            map.add("criterionProgress", criterionProgresses);

            progress.add(map);
        });
        object.add("progress", progress);

        store.save(questSubject.getUniqueId(), object);
    }
}
