package net.chrotos.rpgapi.serialization.config;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.chrotos.rpgapi.selectors.Location;
import net.chrotos.rpgapi.selectors.Player;

import java.lang.reflect.Type;

public class PlayerTypeAdapter implements JsonDeserializer<Player> {
    @Override
    public Player deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        Player.PlayerBuilder builder = Player.builder();

        if (jsonObject.has("id")) {
            builder.id(jsonObject.get("id").getAsString());
        }
        if (jsonObject.has("name")) {
            builder.name(jsonObject.get("name").getAsString());
        }
        if (jsonObject.has("location")) {
            builder.location(context.deserialize(jsonObject.get("location"), Location.class));
        }

        return builder.build();
    }
}
