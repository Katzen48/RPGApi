package net.chrotos.rpgapi.criteria;

import com.google.gson.JsonObject;
import net.chrotos.rpgapi.quests.Quest;
import net.chrotos.rpgapi.subjects.QuestSubject;
import org.bukkit.NamespacedKey;

import java.util.function.Predicate;

// TODO
public class NPCInteractCriteria extends SimpleCriteria<Void, NPCInteractCriteria> {

    public NPCInteractCriteria(Quest quest, NamespacedKey key) {
        super(quest, key);
    }

    @Override
    public CriteriaInstance<Void, NPCInteractCriteria> instanceFromJson(JsonObject json) {
        return null;
    }

    @Override
    public void trigger(QuestSubject subject, Predicate<Void> predicate, CriteriaInstance<Void, NPCInteractCriteria> instance) {

    }
}
