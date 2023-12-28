package net.chrotos.rpgapi.serialization.data;

import lombok.NonNull;
import net.chrotos.rpgapi.datastorage.playerdata.SubjectStorage;
import net.chrotos.rpgapi.quests.QuestGraph;
import net.chrotos.rpgapi.subjects.QuestSubject;

import java.util.UUID;

public interface SubjectSerializer<E extends SubjectStorage> {
    QuestSubject getSubject(@NonNull UUID uniqueId, @NonNull QuestGraph questGraph);
    void saveSubject(@NonNull QuestSubject questSubject);
}
