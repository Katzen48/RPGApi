package net.chrotos.rpgapi.criteria;

import com.google.gson.JsonObject;
import net.chrotos.rpgapi.quests.Quest;
import net.chrotos.rpgapi.quests.QuestStep;
import net.chrotos.rpgapi.subjects.QuestSubject;
import org.bukkit.NamespacedKey;

import java.util.function.Predicate;

public interface Criteria<T, A extends Criteria<T, A>> {
    Quest getQuest();
    NamespacedKey getKey();
    CriteriaInstance<T, A> instanceFromJson(JsonObject json);
    void setQuestStep(QuestStep step);
    void trigger(QuestSubject subject, Predicate<T> predicate, CriteriaInstance<T, A> instance);
}
