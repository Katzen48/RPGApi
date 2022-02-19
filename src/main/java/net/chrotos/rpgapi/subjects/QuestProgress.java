package net.chrotos.rpgapi.subjects;

import lombok.Data;
import net.chrotos.rpgapi.criteria.Criterion;
import net.chrotos.rpgapi.quests.Quest;
import net.chrotos.rpgapi.quests.QuestCriterion;
import net.chrotos.rpgapi.quests.QuestStep;

import java.util.List;

@Data
public class QuestProgress {
    /**
     * The active quest
     */
    private final Quest quest;
    /**
     * The already completed steps
     */
    private final List<QuestStep> completedSteps;
    /**
     * The already completed quest criteria. These are the criteria of the quest steps.
     */
    private final List<QuestCriterion> completedQuestCriteria;
    /**
     * The already completed criteria. These are the criteria of the quest criteria in the single quest steps.
     */
    private final List<Criterion> completedCriteria;
    /**
     * The progress of uncompleted criteria. These are the criteria of the quest criteria in the single quest steps.
     */
    private final List<CriterionProgress<? extends Criterion>> criterionProgresses;
}
