package net.chrotos.rpgapi.serialization.config;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

import java.lang.reflect.Type;

public class BlockDataTypeAdapter implements JsonDeserializer<BlockData> {
    @Override
    public BlockData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        if (!jsonObject.has("data")) {
            return Bukkit.createBlockData(Material.getMaterial(jsonObject.get("material").getAsString()));
        } else {
            return Bukkit.createBlockData(Material.getMaterial(jsonObject.get("material").getAsString()), jsonObject.get("data").getAsString());
        }
    }
}
