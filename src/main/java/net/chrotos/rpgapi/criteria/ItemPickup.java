package net.chrotos.rpgapi.criteria;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import net.chrotos.rpgapi.RPGPlugin;
import net.chrotos.rpgapi.criteria.instances.IntegerInstance;
import net.chrotos.rpgapi.subjects.QuestSubject;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;

@Getter
@SuperBuilder
public class ItemPickup extends ItemCriterion<EntityPickupItemEvent, ItemPickup> {
    public static final NamespacedKey TYPE = new NamespacedKey(RPGPlugin.DEFAULT_NAMESPACE, "item_pickup");

    public boolean check(@NonNull QuestSubject subject, @NonNull EntityPickupItemEvent value, @NonNull IntegerInstance<EntityPickupItemEvent, ItemPickup> instance) {
        if (super.check(subject, value, instance)) {
            return checkIntegerProgress(subject, this.getCount(), getItemStack(value).getAmount(), instance);
        }

        return false;
    }

    @Override
    @NonNull ItemStack getItemStack(@NonNull EntityPickupItemEvent value) {
        return value.getItem().getItemStack();
    }

    protected boolean checkIntegerProgress(@NonNull QuestSubject subject, int required, @NonNull IntegerInstance<EntityPickupItemEvent, ItemPickup> instance) {
        return checkIntegerProgress(subject, required, 1, instance);
    }

    protected boolean checkIntegerProgress(@NonNull QuestSubject subject, int required, int value, @NonNull IntegerInstance<EntityPickupItemEvent, ItemPickup> instance) {
        return checkIntegerProgress(subject, required, value, true, instance);
    }

    protected boolean checkIntegerProgress(@NonNull QuestSubject subject, int required, int value, boolean add, @NonNull IntegerInstance<EntityPickupItemEvent, ItemPickup> instance) {
        if (add) {
            return instance.add(value) >= required;
        } else {
            return instance.deduct(value) >= required;
        }
    }

    @Override
    public CriteriaInstance<EntityPickupItemEvent, ItemPickup> instanceFromJson(JsonObject json) {
        IntegerInstance<EntityPickupItemEvent, ItemPickup> instance = new IntegerInstance<>(this);
        if (json != null && json.has("count")) {
            instance.add(json.get("count").getAsInt());
        }

        return instance;
    }

    public static ItemPickup create(@NonNull JsonObject json, @NonNull JsonDeserializationContext context) {
        ItemPickup.ItemPickupBuilder<?, ?> builder = builder();

        if (json.has("display_name")) {
            json.get("display_name").getAsJsonArray().forEach(element -> builder.displayName(element.getAsString()));
        }

        if (json.has("material")) {
            json.get("material").getAsJsonArray().forEach(element -> builder.material(Material.getMaterial(element.getAsString())));
        }

        if (json.has("custom_model_data")) {
            builder.customModelData(json.get("custom_model_data").getAsInt());
        }

        if (json.has("count")) {
            builder.count(json.get("count").getAsInt());
        }

        return builder.build();
    }
}
