package net.chrotos.rpgapi.criteria;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import net.chrotos.rpgapi.RPGPlugin;
import net.chrotos.rpgapi.criteria.instances.VoidInstance;
import net.chrotos.rpgapi.subjects.QuestSubject;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.AdvancementProgress;

import java.util.List;

@Getter
@Builder
public class AdvancementDone extends SimpleCriteria<AdvancementProgress, AdvancementDone> {
    public static final NamespacedKey TYPE = new NamespacedKey(RPGPlugin.DEFAULT_NAMESPACE, "advancement");

    @Singular("key")
    private final List<NamespacedKey> keys;

    public boolean check(@NonNull QuestSubject subject, @NonNull AdvancementProgress progress) {
        if (!progress.isDone()) {
            return false;
        }

        return keys.contains(progress.getAdvancement().getKey());
    }

    @Override
    public CriteriaInstance<AdvancementProgress, AdvancementDone> instanceFromJson(JsonObject json) {
        return new VoidInstance<>(this);
    }

    @Override
    public void trigger(@NonNull QuestSubject subject, @NonNull AdvancementProgress value, @NonNull CriteriaInstance<AdvancementProgress, AdvancementDone> instance) {
        if (check(subject, value)) {
            this.completed = true;
        }
    }

    public static AdvancementDone create(@NonNull JsonObject json, @NonNull JsonDeserializationContext context) {
        AdvancementDoneBuilder builder = builder();

        json.getAsJsonArray("advancements").forEach(
                jsonElement -> builder.key(NamespacedKey.fromString(jsonElement.getAsString())));

        return builder.build();
    }
}
