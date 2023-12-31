package net.chrotos.rpgapi.criteria;

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
import org.bukkit.advancement.Advancement;

import java.util.List;

@Getter
@Builder
public class AdvancementDone extends SimpleCriteria<Advancement, AdvancementDone> {
    public static final NamespacedKey TYPE = new NamespacedKey(RPGPlugin.DEFAULT_NAMESPACE, "advancement");

    @Singular("key")
    private final List<NamespacedKey> keys;

    public boolean check(@NonNull QuestSubject subject, @NonNull Advancement progress) {
        return keys.contains(progress.getKey());
    }

    @Override
    public CriteriaInstance<Advancement, AdvancementDone> instanceFromJson(JsonObject json) {
        return new VoidInstance<>(this);
    }

    @Override
    public void trigger(@NonNull QuestSubject subject, @NonNull Advancement value, @NonNull CriteriaInstance<Advancement, AdvancementDone> instance) {
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
