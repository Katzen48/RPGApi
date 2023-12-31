package net.chrotos.rpgapi.serialization.config;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.chrotos.rpgapi.selectors.Location;
import net.chrotos.rpgapi.selectors.LocationParameters;

import java.lang.reflect.Type;

public class LocationTypeAdapter implements JsonDeserializer<Location> {
    @Override
    public Location deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        Location.LocationBuilder builder = Location.builder();

        if (jsonObject.has("world")) {
            builder.world(jsonObject.get("world").getAsString());
        }
        if (jsonObject.has("exact")) {
            builder.exact(context.deserialize(jsonObject.get("exact"), LocationParameters.class));
        }
        if (jsonObject.has("min")) {
            builder.min(context.deserialize(jsonObject.get("min"), LocationParameters.class));
        }
        if (jsonObject.has("max")) {
            builder.max(context.deserialize(jsonObject.get("max"), LocationParameters.class));
        }

        return builder.build();
    }
}
