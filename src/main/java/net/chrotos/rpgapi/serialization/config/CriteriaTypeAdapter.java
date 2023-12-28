package net.chrotos.rpgapi.serialization.config;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.chrotos.rpgapi.criteria.Criteria;
import net.chrotos.rpgapi.criteria.CriteriaRegistry;
import org.bukkit.NamespacedKey;

import java.lang.reflect.Type;

public class CriteriaTypeAdapter implements JsonDeserializer<Criteria<?,?>> {
    @Override
    public Criteria<?,?> deserialize(JsonElement jsonElement, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        NamespacedKey key = NamespacedKey.fromString(jsonElement.getAsJsonObject().get("type").getAsString());
        return CriteriaRegistry.get(key).apply(jsonElement.getAsJsonObject());
    }
}
