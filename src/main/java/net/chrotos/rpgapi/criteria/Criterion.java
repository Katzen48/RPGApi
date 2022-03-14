package net.chrotos.rpgapi.criteria;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.chrotos.rpgapi.quests.QuestCriterion;
import net.chrotos.rpgapi.subjects.CriterionProgress;
import net.chrotos.rpgapi.subjects.IntegerCriterionProgress;
import net.chrotos.rpgapi.subjects.QuestProgress;
import net.chrotos.rpgapi.subjects.QuestSubject;

import java.util.function.BiFunction;

@Getter
@Setter
@SuperBuilder()
@NoArgsConstructor
public class Criterion {
    /**
     * The quest criterion, this criterion is part of.
     */
    private QuestCriterion questCriterion;

    protected boolean withProgress(@NonNull QuestSubject subject,
                                @NonNull BiFunction<QuestProgress, CriterionProgress<? extends Criterion>,
                                        Boolean> function) {

        QuestProgress questProgress = subject.getQuestProgress().stream().filter(progress ->
                                                progress.getQuest() == getQuestCriterion().getQuestStep().getQuest())
                                            .findFirst().orElse(null);

        CriterionProgress<? extends Criterion> criterionProgress = questProgress != null ?
                                                            questProgress.getCriterionProgresses().stream()
                                                                    .filter(progress -> progress.getCriterion() == this)
                                                                    .findFirst().orElse(null)
                                                            : null;


        return function.apply(questProgress, criterionProgress);
    }

    protected boolean checkIntegerProgress(@NonNull QuestSubject subject, int required) {
        return checkIntegerProgress(subject, required, 1);
    }

    protected boolean checkIntegerProgress(@NonNull QuestSubject subject, int required, int toAdd) {
        return withProgress(subject, ((questProgress, criterionProgress) -> {
            IntegerCriterionProgress<BlockPlacement> progress = (IntegerCriterionProgress<BlockPlacement>) criterionProgress;

            int current = 0;

            if (questProgress != null && progress != null) {
                current = progress.getValue();
            }
            current += toAdd;

            if (questProgress != null && progress != null) {
                progress.setValue(current);

                if (current >= required) {
                    questProgress.getCriterionProgresses().remove(progress);

                    return true;
                } else {
                    return false;
                }
            } else {
                if (current >= required) {
                    return true;
                }

                if (questProgress == null) {
                    questProgress = QuestProgress.builder()
                            .quest(getQuestCriterion().getQuestStep().getQuest())
                            .build();

                    subject.getQuestProgress().add(questProgress);
                }

                questProgress.getCriterionProgresses().add(new IntegerCriterionProgress<>(this, current));

                return false;
            }
        }));
    }
}
