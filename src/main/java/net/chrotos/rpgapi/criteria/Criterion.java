package net.chrotos.rpgapi.criteria;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.chrotos.rpgapi.gui.QuestLogGuiItem;
import net.chrotos.rpgapi.quests.QuestCriterion;
import net.chrotos.rpgapi.subjects.CriterionProgress;
import net.chrotos.rpgapi.subjects.IntegerCriterionProgress;
import net.chrotos.rpgapi.subjects.QuestProgress;
import net.chrotos.rpgapi.subjects.QuestSubject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

@Getter
@Setter
@SuperBuilder()
@NoArgsConstructor
public class Criterion {
    /**
     * The quest criterion, this criterion is part of.
     */
    private QuestCriterion questCriterion;

    protected QuestLogGuiItem gui;

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

    /**
     * Adds 1 to the progress of <code>subject</code> and checks if <code>required</code> is reached.
     * @param subject the subject
     * @param required the required count/amount to reach
     * @return if the required progress is reached
     */
    protected boolean checkIntegerProgress(@NonNull QuestSubject subject, int required) {
        return checkIntegerProgress(subject, required, 1);
    }

    /**
     * Adds <code>value</code> to the progress of <code>subject</code> and checks if <code>required</code> is reached.
     * @param subject the subject
     * @param required the required count/amount to reach
     * @param value the count/amount to add
     * @return if the required progress is reached
     */
    protected boolean checkIntegerProgress(@NonNull QuestSubject subject, int required, int value) {
        return checkIntegerProgress(subject, required, value, true);
    }

    /**
     * If <code>add == true</code> adds <code>value</code> to the progress of <code>subject</code> and checks,
     * if <code>required</code> is reached.
     *
     * Else checks if <code>value == required</code> and writes it to the progress of subject.
     * This is not for performance, but for displaying the progress to the subject.
     * @param subject the subject
     * @param required the required count/amount to reach
     * @param value the count/amount to add/deduct
     * @param add if the value should be added or deducted
     * @return if the required progress is reached
     */
    protected boolean checkIntegerProgress(@NonNull QuestSubject subject, int required, int value, boolean add) {
        return withProgress(subject, ((questProgress, criterionProgress) -> {
            IntegerCriterionProgress<?> progress = (IntegerCriterionProgress<?>) criterionProgress;

            int current = 0;

            if (add && questProgress != null && progress != null) {
                current = progress.getValue();
            }
            current += value;

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

    protected String getComponentAsPlain(@NonNull Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    protected <E> E orFetch(E first, Supplier<E> supplier) {
        if (first == null) {
            return supplier.get();
        }

        return first;
    }

    protected <E> List<E> orFetch(List<E> first, Supplier<List<E>> supplier) {
        if (first.isEmpty()) {
            return supplier.get();
        }

        return first;
    }
}
