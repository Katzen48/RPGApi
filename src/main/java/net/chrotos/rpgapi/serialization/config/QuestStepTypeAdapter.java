package net.chrotos.rpgapi.serialization.config;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.chrotos.rpgapi.actions.Actions;
import net.chrotos.rpgapi.criteria.Criteria;
import net.chrotos.rpgapi.quests.QuestStep;

import java.lang.reflect.Type;

public class QuestStepTypeAdapter implements JsonDeserializer<QuestStep> {
    @Override
    public QuestStep deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        QuestStep.QuestStepBuilder builder = QuestStep.builder();

        if (jsonObject.has("level")) {
            builder = builder.level(jsonObject.get("level").getAsInt());
        }

        for (JsonElement step : jsonObject.get("criteria").getAsJsonArray()) {
            builder = builder.criterion(context.deserialize(step, QuestStep.class));
        }

        if (jsonObject.has("actions")) {
            builder = builder.actions(context.deserialize(jsonObject.get("actions"), Actions.class));
        }

        return builder.build();
    }
}
