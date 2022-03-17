package net.chrotos.rpgapi.subjects;

import lombok.*;
import net.chrotos.rpgapi.criteria.Criterion;
import net.chrotos.rpgapi.quests.Quest;
import net.chrotos.rpgapi.quests.QuestCriterion;
import net.chrotos.rpgapi.quests.QuestStep;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
@Builder
@RequiredArgsConstructor
public class QuestProgress {
    /**
     * The active quest
     */
    @NonNull
    private final Quest quest;
    /**
     * The already completed steps
     */
    @Builder.Default
    private final List<QuestStep> completedSteps = Collections.synchronizedList(new ArrayList<>());
    /**
     * The already completed quest criteria. These are the criteria of the quest steps.
     */
    @Builder.Default
    private final List<QuestCriterion> completedQuestCriteria = Collections.synchronizedList(new ArrayList<>());
    /**
     * The already completed criteria. These are the criteria of the quest criteria in the single quest steps.
     */
    @Builder.Default
    private final List<Criterion> completedCriteria = Collections.synchronizedList(new ArrayList<>());
    /**
     * The progress of uncompleted criteria. These are the criteria of the quest criteria in the single quest steps.
     */
    @Builder.Default
    private final List<CriterionProgress<? extends Criterion>> criterionProgresses = Collections.synchronizedList(new ArrayList<>());
}
