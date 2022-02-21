package net.chrotos.rpgapi.config;

import lombok.NonNull;
import net.chrotos.rpgapi.quests.Quest;
import net.chrotos.rpgapi.serialization.config.QuestSerializer;

import java.util.List;
import java.util.Map;

/**
 * Uses an implementation of {@link QuestSerializer} to load quests
 */
public interface ConfigStorage {
    void initialize();
    @NonNull
    List<String> getQuestIds();
    Quest getQuest(@NonNull String id);
    @NonNull
    List<Quest> getQuests();
}
