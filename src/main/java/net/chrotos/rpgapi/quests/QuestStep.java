package net.chrotos.rpgapi.quests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Singular;
import net.chrotos.rpgapi.actions.Actions;

import java.util.List;

@Getter
@AllArgsConstructor
public class QuestStep {
    /**
     * Level of this quest. If reached, quest becomes available.
     * When all required quests on this level are completed, the next level becomes available
     */
    private final int level;
    /**
     * If this quest is required, to reach the next quest level
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
