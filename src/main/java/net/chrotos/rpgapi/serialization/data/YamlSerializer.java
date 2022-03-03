package net.chrotos.rpgapi.serialization.data;

import lombok.NonNull;
import net.chrotos.rpgapi.datastorage.SubjectStorage;
import net.chrotos.rpgapi.datastorage.YamlStore;
import net.chrotos.rpgapi.manager.QuestManager;
import net.chrotos.rpgapi.quests.Quest;
import net.chrotos.rpgapi.quests.QuestGraph;
import net.chrotos.rpgapi.quests.QuestLevel;
import net.chrotos.rpgapi.quests.QuestStep;
import net.chrotos.rpgapi.subjects.QuestProgress;
import net.chrotos.rpgapi.subjects.QuestSubject;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

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

        return null; // TODO
    }

    private QuestStep mapQuestStep(int questStep, @NonNull Quest quest) {
        return quest.getSteps().get(questStep);
    }

    @Override
    public void saveSubject(@NonNull QuestSubject questSubject) {

    }
}
