package net.chrotos.rpgapi.quests;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import net.chrotos.rpgapi.actions.Actions;
import net.chrotos.rpgapi.criteria.Criteria;

import java.util.List;

@Getter
@Builder
public class QuestStep {
    /**
     * The quest, this step is part of.
     */
    private Quest quest;
    /**
     * The level of this quest step. Steps of the same level need to be completed, to active the next level. Defaults to 0
     */
    @Builder.Default
    private final int level = 0;
    /**
     * The Criteria for completing this quest step
     */
    @Singular("criterion")
    private final List<Criteria<?, ?>> criteria;
    /**
     * The actions, to be executed after step completion.
     */
    private final Actions actions;

    public void setQuest(@NonNull Quest quest) {
        assert this.quest == null;

        this.quest = quest;
        for (Criteria<?, ?> criterion : criteria) {
            criterion.setQuestStep(this);
        }
    }
}
