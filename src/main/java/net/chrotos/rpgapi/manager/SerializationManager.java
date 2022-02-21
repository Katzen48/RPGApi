package net.chrotos.rpgapi.manager;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.chrotos.rpgapi.config.ConfigStorage;
import net.chrotos.rpgapi.datastorage.SubjectStorage;
import net.chrotos.rpgapi.quests.Quest;
import net.chrotos.rpgapi.subjects.QuestSubject;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class SerializationManager {
    private final ConfigStorage configStorage;
    private final SubjectStorage subjectStorage;

    public final Quest getQuest(@NonNull String id) {
        return configStorage.getQuest(id);
    }

    public final List<Quest> getQuests() {
        return configStorage.getQuests();
    }

    public final QuestSubject getSubject(@NonNull UUID uniqueId) {
        return subjectStorage.getSubject(uniqueId);
    }
}
