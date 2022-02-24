package net.chrotos.rpgapi.manager;

import com.google.common.collect.Maps;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import net.chrotos.rpgapi.config.ConfigStorage;
import net.chrotos.rpgapi.datastorage.SubjectStorage;
import net.chrotos.rpgapi.quests.Quest;
import net.chrotos.rpgapi.quests.QuestGraph;
import net.chrotos.rpgapi.subjects.QuestSubject;

import java.util.*;
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

    public QuestSubject getQuestSubject(UUID uniqueId) {
        return subjectHashMap.get(uniqueId);
    }

    @Synchronized
    public void addQuestSubject(@NonNull QuestSubject questSubject) {
        if (!subjectHashMap.containsKey(questSubject.getUniqueId())) {
            subjectHashMap.put(questSubject.getUniqueId(), questSubject);
        }

        throw new IllegalStateException("Quest Subject already exists");
    }

    @Synchronized
    public QuestSubject loadQuestSubject(@NonNull UUID uniqueId) {
        return subjectStorage.getSubject(uniqueId);
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
}
