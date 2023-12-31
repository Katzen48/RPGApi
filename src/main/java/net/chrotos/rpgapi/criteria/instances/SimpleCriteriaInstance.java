package net.chrotos.rpgapi.criteria.instances;

import lombok.NonNull;
import net.chrotos.rpgapi.criteria.Criteria;
import net.chrotos.rpgapi.criteria.CriteriaInstance;
import net.chrotos.rpgapi.subjects.QuestSubject;

public abstract class SimpleCriteriaInstance<T, A extends Criteria<T, A>> implements CriteriaInstance<T, A> {
    private final A criteria;

    public SimpleCriteriaInstance(@NonNull A criteria) {
        this.criteria = criteria;
    }

    @Override
    public A getCriteria() {
        return criteria;
    }

    @Override
    public void trigger(@NonNull QuestSubject subject, @NonNull T value) {
        criteria.trigger(subject, value, this);
    }
}
