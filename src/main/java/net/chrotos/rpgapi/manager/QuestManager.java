package net.chrotos.rpgapi.manager;

import com.google.common.collect.Maps;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.Synchronized;
import net.chrotos.rpgapi.config.ConfigStorage;
import net.chrotos.rpgapi.datastorage.SubjectStorage;
import net.chrotos.rpgapi.quests.Quest;
import net.chrotos.rpgapi.quests.QuestGraph;
import net.chrotos.rpgapi.subjects.QuestSubject;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;
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
    public void onPlayerJoin(PlayerJoinEvent event) {
        addQuestSubject(getQuestSubject(event.getPlayer().getUniqueId()));
    }

    @Synchronized
    public void onPlayerQuit(PlayerQuitEvent event) {
        saveQuestSubject(event.getPlayer().getUniqueId());
        removeQuestSubject(getQuestSubject(event.getPlayer().getUniqueId()));
    }
}
