package net.chrotos.rpgapi.serialization.data;

import lombok.NonNull;
import net.chrotos.rpgapi.datastorage.SubjectStorage;
import net.chrotos.rpgapi.subjects.QuestSubject;

import java.util.UUID;

public interface SubjectSerializer<E extends SubjectStorage> {
    /**
     * Has to be called by the {@link SubjectStorage} implementation in the initialize method
     */
    void initialize(@NonNull E subjectStorage);
    QuestSubject getSubject(@NonNull UUID uniqueId);
    void saveSubject(@NonNull QuestSubject questSubject);
}
