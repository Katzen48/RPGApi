package net.chrotos.rpgapi.serialization.config;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.chrotos.rpgapi.selectors.LocationParameters;

import java.lang.reflect.Type;

public class LocationParametersTypeAdapter implements JsonDeserializer<LocationParameters> {
    @Override
    public LocationParameters deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        LocationParameters.LocationParametersBuilder builder = LocationParameters.builder();

        builder.x(jsonObject.get("x").getAsInt());
        builder.y(jsonObject.get("y").getAsInt());
        builder.z(jsonObject.get("z").getAsInt());

        return builder.build();
    }
}
