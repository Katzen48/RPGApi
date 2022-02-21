package net.chrotos.rpgapi.datastorage;

import lombok.NonNull;
import net.chrotos.rpgapi.subjects.QuestSubject;
import net.chrotos.rpgapi.serialization.data.SubjectSerializer;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * Uses an implementation of {@link SubjectSerializer} to load subjects
 */
public interface SubjectStorage {
    void initialize();
    QuestSubject getSubject(@NonNull UUID uniqueId);
    void saveSubject(@NonNull QuestSubject questSubject);
}
