package net.chrotos.rpgapi.serialization.config;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.papermc.paper.advancement.AdvancementDisplay;
import net.chrotos.rpgapi.actions.Actions;
import net.chrotos.rpgapi.actions.initialization.InitializationActions;
import net.chrotos.rpgapi.quests.Quest;
import net.chrotos.rpgapi.quests.QuestStep;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;

import java.lang.reflect.Type;

public class QuestTypeAdapter implements JsonDeserializer<Quest> {
    @Override
    public Quest deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        Quest.QuestBuilder builder = Quest.builder();

        builder = builder.description(context.deserialize(jsonObject.get("description"), Component.class))
                .title(context.deserialize(jsonObject.get("title"), Component.class))
                .frame(jsonObject.has("frame") ? AdvancementDisplay.Frame.valueOf(jsonObject.get("frame").getAsString().toUpperCase()) : AdvancementDisplay.Frame.TASK)
                .hidden(jsonObject.has("hidden") && jsonObject.get("hidden").getAsBoolean())
                .announce(jsonObject.has("announce") && jsonObject.get("announce").getAsBoolean())
                .subTitle(jsonObject.has("sub_title") ? context.deserialize(jsonObject.get("sub_title"), Component.class) : null)
                .parent(jsonObject.has("parent") ? NamespacedKey.fromString(jsonObject.get("parent").getAsString()) : null);

        for (JsonElement step : jsonObject.get("steps").getAsJsonArray()) {
            builder = builder.step(context.deserialize(step, QuestStep.class));
        }

        if (jsonObject.has("actions")) {
            builder = builder.actions(context.deserialize(jsonObject.get("actions"), Actions.class));
        }

        if (jsonObject.has("initialization_actions")) {
            builder = builder.initializationActions(context.deserialize(jsonObject.get("initialization_actions"), InitializationActions.class));
        }

        return builder.build();
    }
}
