package net.chrotos.rpgapi.criteria.instances;

import com.google.gson.JsonObject;
import lombok.NonNull;
import net.chrotos.rpgapi.criteria.Criteria;

import java.util.concurrent.atomic.AtomicBoolean;

public class BooleanInstance<T, A extends Criteria<T, A>> extends SimpleCriteriaInstance<T, A> {
    private AtomicBoolean value = new AtomicBoolean();

    public BooleanInstance(@NonNull A criteria) {
        super(criteria);
    }

    public boolean getValue() {
        return value.get();
    }

    public void setValue(boolean value) {
        this.value.set(value);
    }

    public boolean toggle() {
        return value.getAndSet(!value.get());
    }

    @Override
    public JsonObject serialize() {
        return null;
    }
}
