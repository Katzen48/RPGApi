package net.chrotos.rpgapi.datastorage;

import lombok.NonNull;
import net.chrotos.rpgapi.quests.QuestGraph;
import net.chrotos.rpgapi.serialization.data.SubjectSerializer;
import net.chrotos.rpgapi.subjects.QuestSubject;

import java.util.UUID;
import java.util.function.Function;

/**
 * Uses an implementation of {@link SubjectSerializer} to load subjects
 */
public interface SubjectStorage {
    void initialize(@NonNull Function<UUID, ? extends QuestSubject> subjectFunction);
    QuestSubject getSubject(@NonNull UUID uniqueId, @NonNull QuestGraph questGraph);
    void saveSubject(@NonNull QuestSubject questSubject);
}
