package net.chrotos.rpgapi.criteria;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import net.chrotos.rpgapi.RPGPlugin;
import net.chrotos.rpgapi.criteria.instances.VoidInstance;
import net.chrotos.rpgapi.selectors.LocationParameters;
import net.chrotos.rpgapi.subjects.QuestSubject;
import org.bukkit.NamespacedKey;

@Getter
@Builder
public class Location extends SimpleCriteria<org.bukkit.Location, Location> {
    public static final NamespacedKey TYPE = new NamespacedKey(RPGPlugin.DEFAULT_NAMESPACE, "location");

    @Builder.Default
    private final String world = "world";
    private final LocationParameters exact;
    private final LocationParameters min;
    private final LocationParameters max;

    public boolean check(@NonNull QuestSubject subject, @NonNull org.bukkit.Location object) {
        if (world != null && !object.getWorld().getName().equals(world)) {
            return false;
        }

        int x = object.getBlockX();
        int y = object.getBlockY();
        int z = object.getBlockZ();

        if (exact != null) {
            return exact.equal(x, y, z);
        }

        return LocationParameters.between(min, max, x, y, z);
    }

    @Override
    public CriteriaInstance<org.bukkit.Location, Location> instanceFromJson(JsonObject json) {
        return new VoidInstance<>(this);
    }

    @Override
    public void trigger(@NonNull QuestSubject subject, org.bukkit.@NonNull Location value, @NonNull CriteriaInstance<org.bukkit.Location, Location> instance) {
        if (check(subject, value)) {
            this.completed = true;
        }
    }

    public static Location create(@NonNull JsonObject json, @NonNull JsonDeserializationContext context) {
        Location.LocationBuilder builder = builder();

        if (json.has("world")) {
            builder.world(json.get("world").getAsString());
        }
        if (json.has("exact")) {
            builder.exact(context.deserialize(json.get("exact"), LocationParameters.class));
        }
        if (json.has("min")) {
            builder.min(context.deserialize(json.get("min"), LocationParameters.class));
        }
        if (json.has("max")) {
            builder.max(context.deserialize(json.get("max"), LocationParameters.class));
        }

        return builder.build();
    }
}
