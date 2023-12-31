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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

@Getter
@SuperBuilder
public class ItemUse extends ItemCriterion<PlayerInteractEvent, ItemUse> {
    public static final NamespacedKey TYPE = new NamespacedKey(RPGPlugin.DEFAULT_NAMESPACE, "item_use");

    public boolean check(@NonNull QuestSubject subject, @NonNull PlayerInteractEvent value, @NonNull IntegerInstance<PlayerInteractEvent, ItemUse> instance) {
        if (super.check(subject, value, instance)) {
            return checkIntegerProgress(subject, this.getCount(), instance);
        }

        return false;
    }

    @Override
    @NonNull ItemStack getItemStack(@NonNull PlayerInteractEvent value) {
        return value.getItem();
    }

    protected boolean checkIntegerProgress(@NonNull QuestSubject subject, int required, @NonNull IntegerInstance<PlayerInteractEvent, ItemUse> instance) {
        return checkIntegerProgress(subject, required, 1, instance);
    }

    protected boolean checkIntegerProgress(@NonNull QuestSubject subject, int required, int value, @NonNull IntegerInstance<PlayerInteractEvent, ItemUse> instance) {
        return checkIntegerProgress(subject, required, value, true, instance);
    }

    protected boolean checkIntegerProgress(@NonNull QuestSubject subject, int required, int value, boolean add, @NonNull IntegerInstance<PlayerInteractEvent, ItemUse> instance) {
        if (add) {
            return instance.add(value) >= required;
        } else {
            return instance.deduct(value) >= required;
        }
    }

    @Override
    public CriteriaInstance<PlayerInteractEvent, ItemUse> instanceFromJson(JsonObject json) {
        IntegerInstance<PlayerInteractEvent, ItemUse> instance = new IntegerInstance<>(this);
        if (json != null && json.has("count")) {
            instance.add(json.get("count").getAsInt());
        }

        return instance;
    }

    public static ItemUse create(@NonNull JsonObject json, @NonNull JsonDeserializationContext context) {
        ItemUse.ItemUseBuilder<?, ?> builder = builder();

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
