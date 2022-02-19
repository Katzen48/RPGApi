package net.chrotos.rpgapi.subjects;

import lombok.Data;
import lombok.NonNull;
import net.chrotos.rpgapi.criteria.Criterion;

@Data
public class CriterionProgress<E extends Criterion> {
    /**
     * The criterion, that has been progressed.
     */
    @NonNull
    private final E criterion;
}
