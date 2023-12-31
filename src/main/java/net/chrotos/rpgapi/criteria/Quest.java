package net.chrotos.rpgapi.criteria;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import lombok.*;
import net.chrotos.rpgapi.RPGPlugin;
import net.chrotos.rpgapi.criteria.instances.VoidInstance;
import net.chrotos.rpgapi.subjects.QuestSubject;
import org.bukkit.NamespacedKey;

@Getter
@Builder
public class Quest extends SimpleCriteria<net.chrotos.rpgapi.quests.Quest, Quest> {
    public static final NamespacedKey TYPE = new NamespacedKey(RPGPlugin.DEFAULT_NAMESPACE, "quest");

    private final NamespacedKey quest;

    @Override
    public CriteriaInstance<net.chrotos.rpgapi.quests.Quest, Quest> instanceFromJson(JsonObject json) {
        return new VoidInstance<>(this);
    }

    @Override
    public void trigger(@NonNull QuestSubject subject, net.chrotos.rpgapi.quests.@NonNull Quest value, @NonNull CriteriaInstance<net.chrotos.rpgapi.quests.Quest, Quest> instance) {
        if (value.getKey().equals(quest)) {
            this.completed = true;
        }
    }

    public static Quest create(@NonNull JsonObject json, @NonNull JsonDeserializationContext context) {
        Quest.QuestBuilder builder = builder();

        builder.quest(NamespacedKey.fromString(json.get("quest").getAsString()));

        return builder.build();
    }
}
