package net.chrotos.rpgapi.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class CounterLock {
    private static final Map<UUID, AtomicInteger> COUNTER = Collections.synchronizedMap(new HashMap<>());

    public static int increment(UUID uuid) {
        return get(uuid).incrementAndGet();
    }

    public static int decrement(UUID uuid) {
        return get(uuid).decrementAndGet();
    }

    public static int getValue(UUID uuid) {
        return get(uuid).get();
    }

    public static void reset(UUID uuid) {
        COUNTER.remove(uuid);
    }

    private static AtomicInteger get(UUID uuid) {
        return COUNTER.computeIfAbsent(uuid, (id) -> new AtomicInteger());
    }
}
