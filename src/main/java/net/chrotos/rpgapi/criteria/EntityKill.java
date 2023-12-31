package net.chrotos.rpgapi.criteria;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import net.chrotos.rpgapi.RPGPlugin;
import net.chrotos.rpgapi.criteria.instances.IntegerInstance;
import net.chrotos.rpgapi.selectors.Location;
import net.chrotos.rpgapi.subjects.QuestSubject;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDeathEvent;

@Getter
@SuperBuilder
public class EntityKill extends EntityCriterion<EntityDeathEvent, EntityKill> {
    public static final NamespacedKey TYPE = new NamespacedKey(RPGPlugin.DEFAULT_NAMESPACE, "entity_kill");

    @Builder.Default
    private final Integer count = 1;

    @Override
    public boolean check(@NonNull QuestSubject subject, @NonNull EntityDeathEvent object, @NonNull IntegerInstance<EntityDeathEvent, EntityKill> instance) {
        if (!super.check(subject, object, instance)) {
            return false;
        }

        return checkIntegerProgress(subject, count, instance);
    }

    protected boolean checkIntegerProgress(@NonNull QuestSubject subject, int required, @NonNull IntegerInstance<EntityDeathEvent, EntityKill> instance) {
        return checkIntegerProgress(subject, required, 1, instance);
    }

    protected boolean checkIntegerProgress(@NonNull QuestSubject subject, int required, int value, @NonNull IntegerInstance<EntityDeathEvent, EntityKill> instance) {
        return checkIntegerProgress(subject, required, value, true, instance);
    }

    protected boolean checkIntegerProgress(@NonNull QuestSubject subject, int required, int value, boolean add, @NonNull IntegerInstance<EntityDeathEvent, EntityKill> instance) {
        if (add) {
            return instance.add(value) >= required;
        } else {
            return instance.deduct(value) >= required;
        }
    }

    @Override
    public CriteriaInstance<EntityDeathEvent, EntityKill> instanceFromJson(JsonObject json) {
        IntegerInstance<EntityDeathEvent, EntityKill> instance = new IntegerInstance<>(this);

        if (json != null && json.has("damage")) {
            instance.add(json.get("damage").getAsInt());
        }

        return instance;
    }

    @Override
    public void trigger(@NonNull QuestSubject subject, @NonNull EntityDeathEvent value, @NonNull CriteriaInstance<EntityDeathEvent, EntityKill> instance) {
        if (instance instanceof IntegerInstance<EntityDeathEvent, EntityKill> integerInstance) {
            if (check(subject, value, integerInstance)) {
                this.completed = true;
            }
        }
    }

    @Override
    @NonNull Entity getEntity(@NonNull EntityDeathEvent value) {
        return value.getEntity();
    }

    public static EntityKill create(@NonNull JsonObject json, @NonNull JsonDeserializationContext context) {
        EntityKill.EntityKillBuilder<?, ?> builder = builder();

        if (json.has("count")) {
            builder.count(json.get("count").getAsInt());
        }

        if (json.has("entity")) {
            JsonObject entity = json.getAsJsonObject("entity");

            if (entity.has("id")) {
                builder.id(entity.get("id").getAsString());
            }
            if (entity.has("type")) {
                builder.type(EntityType.valueOf(entity.get("type").getAsString()));
            }
            if (entity.has("display_name")) {
                builder.displayName(entity.get("display_name").getAsString());
            }
            if (entity.has("location")) {
                builder.location(context.deserialize(entity.get("location"), Location.class));
            }
        }

        return builder.build();
    }
}
