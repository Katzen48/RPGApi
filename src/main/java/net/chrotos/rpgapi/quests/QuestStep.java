package net.chrotos.rpgapi.quests;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.Singular;
import net.chrotos.rpgapi.actions.Actions;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class QuestStep {
    /**
     * The quest, this step is part of.
     */
    @Setter
    private Quest quest;
    /**
     * If this quest is required, to complete the quest
     */
    private final boolean required;
    /**
     * The Criteria for completing this quest step
     */
    @Singular("criterion")
    private final List<QuestCriterion> criteria;
    /**
     * The actions, to be executed after step completion.
     */
    private final Actions actions;
}
