package net.chrotos.rpgapi.criteria.instances;

import com.google.gson.JsonObject;
import lombok.NonNull;
import net.chrotos.rpgapi.criteria.Criteria;

public class VoidInstance<T, A extends Criteria<T, A>> extends SimpleCriteriaInstance<T, A> {
    public VoidInstance(@NonNull A criteria) {
        super(criteria);
    }

    @Override
    public JsonObject serialize() {
        return null;
    }
}
