package net.chrotos.rpgapi.criteria;

import com.google.gson.JsonObject;
import org.bukkit.NamespacedKey;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

public class CriteriaRegistry {
    private static final Map<NamespacedKey, Function<JsonObject, Criteria<?, ?>>> CRITERIA = new IdentityHashMap<>();

    public static <T extends Function<JsonObject, Criteria<?, ?>>> T register(NamespacedKey key, T criteria) {
        if (CRITERIA.putIfAbsent(key, criteria) != null) {
            throw new IllegalArgumentException("Criteria with key " + key + " already exists!");
        }

        return criteria;
    }

    public static Function<JsonObject, Criteria<?, ?>> get(NamespacedKey key) {
        return CRITERIA.get(key);
    }
}
