package net.chrotos.rpgapi.serialization.config;

import lombok.NonNull;
import net.chrotos.rpgapi.config.ConfigStorage;
import net.chrotos.rpgapi.quests.Quest;

import java.util.List;

public interface QuestSerializer<E extends ConfigStorage> {
    Quest getQuest(@NonNull String id);
    @NonNull
    List<Quest> getQuests();
}
