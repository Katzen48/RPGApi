package net.chrotos.rpgapi.criteria;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.chrotos.rpgapi.quests.Quest;
import net.chrotos.rpgapi.quests.QuestStep;

@Getter
public abstract class SimpleCriteria<T, A extends Criteria<T, A>> implements Criteria<T, A> {
    protected final Quest quest;
    protected final String id;
    @Setter
    @NonNull
    protected QuestStep questStep;
    protected boolean completed;

    protected SimpleCriteria(@NonNull Quest quest, @NonNull String id) {
        this.quest = quest;
        this.id = id;
    }
}
