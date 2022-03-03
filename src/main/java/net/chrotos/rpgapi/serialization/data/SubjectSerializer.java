package net.chrotos.rpgapi.serialization.data;

import lombok.NonNull;
import net.chrotos.rpgapi.datastorage.SubjectStorage;
import net.chrotos.rpgapi.manager.QuestManager;
import net.chrotos.rpgapi.quests.QuestGraph;
import net.chrotos.rpgapi.subjects.QuestSubject;

import java.util.UUID;
import java.util.function.Function;

public interface SubjectSerializer<E extends SubjectStorage> {
    /**
     * Has to be called by the {@link SubjectStorage} implementation in the initialize method
     */
    void initialize(@NonNull E subjectStorage, @NonNull Function<UUID, ? extends QuestSubject> subjectFunction);
    QuestSubject getSubject(@NonNull UUID uniqueId, @NonNull QuestGraph questGraph);
    void saveSubject(@NonNull QuestSubject questSubject);
}
