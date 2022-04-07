package net.chrotos.rpgapi.subjects;

import lombok.*;
import net.chrotos.rpgapi.criteria.Criterion;
import net.chrotos.rpgapi.quests.Quest;
import net.chrotos.rpgapi.quests.QuestCriterion;
import net.chrotos.rpgapi.quests.QuestStep;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
     * The active quest steps
     */
    @Builder.Default
    private final List<QuestStep> activeQuestSteps = Collections.synchronizedList(new CopyOnWriteArrayList<>());
    /**
     * The already completed steps
     */
    @Builder.Default
    private final List<QuestStep> completedSteps = Collections.synchronizedList(new CopyOnWriteArrayList<>());
    /**
     * The already completed quest criteria. These are the criteria of the quest steps.
     */
    @Builder.Default
    private final List<QuestCriterion> completedQuestCriteria = Collections.synchronizedList(new CopyOnWriteArrayList<>());
    /**
     * The already completed criteria. These are the criteria of the quest criteria in the single quest steps.
     */
    @Builder.Default
    private final List<Criterion> completedCriteria = Collections.synchronizedList(new CopyOnWriteArrayList<>());
    /**
     * The progress of uncompleted criteria. These are the criteria of the quest criteria in the single quest steps.
     */
    @Builder.Default
    private final List<CriterionProgress<? extends Criterion>> criterionProgresses = Collections.synchronizedList(new CopyOnWriteArrayList<>());
}
