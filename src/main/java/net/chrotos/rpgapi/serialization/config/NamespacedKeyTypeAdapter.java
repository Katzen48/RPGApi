package net.chrotos.rpgapi.serialization.config;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.bukkit.NamespacedKey;

import java.lang.reflect.Type;

public class NamespacedKeyTypeAdapter implements JsonDeserializer<NamespacedKey> {
@Override
    public NamespacedKey deserialize(JsonElement jsonElement, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return NamespacedKey.fromString(jsonElement.getAsString());
    }
}
