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

@RequiredArgsConstructor
public class QuestManager {
    private QuestGraph questGraph;
    private final Map<UUID, QuestSubject> subjectHashMap = Maps.newConcurrentMap();
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
            questGraph = QuestGraph.generate(loadQuests());
        }

        return questGraph;
    }

    @Synchronized
    public Quest loadQuest(@NonNull String id) {
        return configStorage.getQuest(id);
    }

    @Synchronized
    @NonNull
    public List<Quest> loadQuests() {
        return configStorage.getQuests();
    }
}
