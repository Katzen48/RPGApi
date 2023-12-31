package net.chrotos.rpgapi.criteria;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import net.chrotos.rpgapi.RPGPlugin;
import net.chrotos.rpgapi.criteria.instances.VoidInstance;
import net.chrotos.rpgapi.npc.NPC;
import net.chrotos.rpgapi.subjects.QuestSubject;
import org.bukkit.NamespacedKey;

import java.util.function.Predicate;

@Getter
@Builder
public class NPCInteract extends SimpleCriteria<NPC, NPCInteract> {
    public static final NamespacedKey TYPE = new NamespacedKey(RPGPlugin.DEFAULT_NAMESPACE, "npc_interact");

    private final Predicate<NPC> predicate;

    @Override
    public CriteriaInstance<NPC, NPCInteract> instanceFromJson(JsonObject json) {
        return new VoidInstance<>(this);
    }

    @Override
    public void trigger(@NonNull QuestSubject subject, @NonNull NPC value, @NonNull CriteriaInstance<NPC, NPCInteract> instance) {
        if (predicate.test(value)) {
            this.completed = true;
        }
    }

    public static NPCInteract create(@NonNull JsonObject json, @NonNull JsonDeserializationContext context) {
        // TODO

        NPCInteract.NPCInteractBuilder builder = builder();

        return builder.build();
    }
}
