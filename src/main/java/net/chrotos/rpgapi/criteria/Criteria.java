package net.chrotos.rpgapi.criteria;

import com.google.gson.JsonObject;
import lombok.NonNull;
import net.chrotos.rpgapi.quests.Quest;
import net.chrotos.rpgapi.quests.QuestStep;
import net.chrotos.rpgapi.subjects.QuestSubject;

public interface Criteria<T, A extends Criteria<T, A>> {
    Quest getQuest();
    String getId();
    CriteriaInstance<T, A> instanceFromJson(JsonObject json);
    boolean isCompleted();
    void setQuestStep(QuestStep step);
    void trigger(@NonNull QuestSubject subject, @NonNull T value, @NonNull CriteriaInstance<T, A> instance);
}
