package net.chrotos.rpgapi.criteria.instances;

import com.google.gson.JsonObject;
import lombok.NonNull;
import net.chrotos.rpgapi.criteria.Criteria;

import java.util.concurrent.atomic.AtomicInteger;

public class IntegerInstance<T, A extends Criteria<T, A>> extends SimpleCriteriaInstance<T, A> {
    private final AtomicInteger integer = new AtomicInteger();

    public IntegerInstance(@NonNull A criteria) {
        super(criteria);
    }

    public int add(int value) {
        return integer.addAndGet(value);
    }

    public int increment() {
        return add(1);
    }

    public int decrement() {
        return add(-1);
    }

    public int deduct(int value) {
        return add(-value);
    }

    public int get() {
        return integer.get();
    }

    @Override
    public JsonObject serialize() {
        return null;
    }
}
