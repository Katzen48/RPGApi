package net.chrotos.rpgapi.subjects;

import lombok.Getter;
import lombok.Setter;
import net.chrotos.rpgapi.criteria.Criterion;

@Getter
@Setter
public class IntegerCriterionProgress<E extends Criterion> extends CriterionProgress<E> {
    /**
     * The specific progress.
     */
    private int value;

    public IntegerCriterionProgress(E criterion) {
        this(criterion, 0);
    }

    public IntegerCriterionProgress(E criterion, int value) {
        super(criterion);
        this.value = value;
    }
}
