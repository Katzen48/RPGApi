package net.chrotos.rpgapi.serialization.config;

import lombok.NonNull;
import net.chrotos.rpgapi.config.ConfigStorage;
import net.chrotos.rpgapi.quests.Quest;

import java.util.List;

public interface QuestSerializer<E extends ConfigStorage> {
    /**
     * Has to be called by the {@link ConfigStorage} implementation in the initialize method
     */
    void initialize(@NonNull E configStorage);
    Quest getQuest(@NonNull String id);
    @NonNull
    List<Quest> getQuests();
}
