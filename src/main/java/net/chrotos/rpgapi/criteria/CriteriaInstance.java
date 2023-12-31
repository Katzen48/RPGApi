package net.chrotos.rpgapi.criteria;

import com.google.gson.JsonObject;
import lombok.NonNull;
import net.chrotos.rpgapi.subjects.QuestSubject;

public interface CriteriaInstance<T, A extends Criteria<T, A>> {
    A getCriteria();
    JsonObject serialize();
    void trigger(@NonNull QuestSubject subject, @NonNull T value);
}
