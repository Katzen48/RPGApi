package net.chrotos.rpgapi.datastorage;

import lombok.NonNull;
import net.chrotos.rpgapi.serialization.data.SubjectSerializer;
import net.chrotos.rpgapi.subjects.QuestSubject;

import java.util.UUID;

/**
 * Uses an implementation of {@link SubjectSerializer} to load subjects
 */
public interface SubjectStorage {
    void initialize();
    QuestSubject getSubject(@NonNull UUID uniqueId);
    void saveSubject(@NonNull QuestSubject questSubject);
}
