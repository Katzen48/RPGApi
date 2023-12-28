package net.chrotos.rpgapi.criteria;

import lombok.Getter;
import lombok.NonNull;
import net.chrotos.rpgapi.quests.Quest;
import net.chrotos.rpgapi.subjects.QuestSubject;
import org.bukkit.NamespacedKey;

import java.util.function.Predicate;

@Getter
public abstract class SimpleCriteria<T, A extends Criteria<T, A>> implements Criteria<T, A> {
    protected final Quest quest;
    protected final NamespacedKey key;

    protected SimpleCriteria(@NonNull Quest quest, @NonNull NamespacedKey key) {
        this.quest = quest;
        this.key = key;
    }
}
