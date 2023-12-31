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
import org.bukkit.event.entity.EntityDamageEvent;

@Getter
@SuperBuilder
public class EntityDamage extends EntityCriterion<EntityDamageEvent, EntityDamage> {
    public static final NamespacedKey TYPE = new NamespacedKey(RPGPlugin.DEFAULT_NAMESPACE, "entity_damage");

    @Builder.Default
    private final Integer damage = 1;

    @Override
    public boolean check(@NonNull QuestSubject subject, @NonNull EntityDamageEvent object, @NonNull IntegerInstance<EntityDamageEvent, EntityDamage> instance) {
        if (!super.check(subject, object, instance)) {
            return false;
        }

        return checkIntegerProgress(subject, damage, (int) object.getFinalDamage(), instance);
    }

    protected boolean checkIntegerProgress(@NonNull QuestSubject subject, int required, int value, @NonNull IntegerInstance<EntityDamageEvent, EntityDamage> instance) {
        return checkIntegerProgress(subject, required, value, true, instance);
    }

    protected boolean checkIntegerProgress(@NonNull QuestSubject subject, int required, int value, boolean add, @NonNull IntegerInstance<EntityDamageEvent, EntityDamage> instance) {
        if (add) {
            return instance.add(value) >= required;
        } else {
            return instance.deduct(value) >= required;
        }
    }

    @Override
    public CriteriaInstance<EntityDamageEvent, EntityDamage> instanceFromJson(JsonObject json) {
        IntegerInstance<EntityDamageEvent, EntityDamage> instance = new IntegerInstance<>(this);

        if (json != null && json.has("damage")) {
            instance.add(json.get("damage").getAsInt());
        }

        return instance;
    }

    @Override
    public void trigger(@NonNull QuestSubject subject, @NonNull EntityDamageEvent value, @NonNull CriteriaInstance<EntityDamageEvent, EntityDamage> instance) {
        if (instance instanceof IntegerInstance<EntityDamageEvent, EntityDamage> integerInstance) {
            if (check(subject, value, integerInstance)) {
                this.completed = true;
            }
        }
    }

    @Override
    @NonNull Entity getEntity(@NonNull EntityDamageEvent value) {
        return value.getEntity();
    }

    public static EntityDamage create(@NonNull JsonObject json, @NonNull JsonDeserializationContext context) {
        EntityDamage.EntityDamageBuilder<?, ?> builder = builder();

        if (json.has("damage")) {
            builder.damage(json.get("damage").getAsInt());
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
