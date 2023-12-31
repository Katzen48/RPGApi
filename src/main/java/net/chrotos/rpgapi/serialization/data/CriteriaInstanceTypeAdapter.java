package net.chrotos.rpgapi.serialization.data;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.chrotos.rpgapi.RPGPlugin;
import net.chrotos.rpgapi.criteria.Criteria;
import net.chrotos.rpgapi.criteria.CriteriaInstance;
import org.bukkit.NamespacedKey;

import java.lang.reflect.Type;

public class CriteriaInstanceTypeAdapter implements JsonDeserializer<CriteriaInstance<?, ? extends Criteria<?, ?>>>, JsonSerializer<CriteriaInstance<?, ? extends Criteria<?, ?>>> {
    @Override
    public CriteriaInstance<?, ? extends Criteria<?, ?>> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        NamespacedKey questKey = NamespacedKey.fromString(json.getAsJsonObject().get("quest").getAsString());
        String criteriaId = json.getAsJsonObject().get("trigger").getAsString();

        Criteria<?,?> criteria = RPGPlugin.getInstance().getQuestManager().getQuest(questKey).getCriteria(criteriaId);

        return criteria.instanceFromJson(json.getAsJsonObject().getAsJsonObject("data"));
    }

    @Override
    public JsonElement serialize(CriteriaInstance<?, ? extends Criteria<?, ?>> src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("quest", src.getCriteria().getQuest().getKey().toString());
        jsonObject.addProperty("trigger", src.getCriteria().getId());
        jsonObject.add("data", src.serialize());

        return jsonObject;
    }
}
