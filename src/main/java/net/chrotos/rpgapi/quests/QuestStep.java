package net.chrotos.rpgapi.quests;

import lombok.*;
import net.chrotos.rpgapi.actions.Actions;

import java.util.List;

@Getter
@Builder
public class QuestStep {
    /**
     * The quest, this step is part of.
     */
    private Quest quest;
    /**
     * If this quest is required, to complete the quest. Defaults to true.
     */
    @Builder.Default
    private final boolean required = true;
    /**
     * The Criteria for completing this quest step
     */
    @Singular("criterion")
    private final List<QuestCriterion> criteria;
    /**
     * The actions, to be executed after step completion.
     */
    private final Actions actions;

    public void setQuest(@NonNull Quest quest) {
        assert this.quest == null;

        this.quest = quest;
        for (QuestCriterion criterion : criteria) {
            criterion.setQuestStep(this);
        }
    }
}
