package net.chrotos.rpgapi.criteria;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import net.chrotos.rpgapi.RPGPlugin;
import net.chrotos.rpgapi.criteria.instances.IntegerInstance;
import net.chrotos.rpgapi.selectors.Player;
import net.chrotos.rpgapi.subjects.QuestSubject;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

@Getter
@SuperBuilder
public class Inventory extends ItemCriterion<ItemStack, Inventory> {
    public static final NamespacedKey TYPE = new NamespacedKey(RPGPlugin.DEFAULT_NAMESPACE, "inventory");
    
    private final Player player;

    public boolean check(@NonNull QuestSubject subject, @NonNull ItemStack value, @NonNull IntegerInstance<ItemStack, Inventory> instance) {
        if (super.check(subject, value, instance)) {
            if (player != null && !player.applies(subject)) {
                return false;
            }

            return checkIntegerProgress(subject, this.getCount(), value.getAmount(), instance);
        }

        return false;
    }

    @Override
    @NonNull ItemStack getItemStack(@NonNull ItemStack value) {
        return value;
    }

    protected boolean checkIntegerProgress(@NonNull QuestSubject subject, int required, int value, @NonNull IntegerInstance<ItemStack, Inventory> instance) {
        return checkIntegerProgress(subject, required, value, true, instance);
    }

    protected boolean checkIntegerProgress(@NonNull QuestSubject subject, int required, int value, boolean add, @NonNull IntegerInstance<ItemStack, Inventory> instance) {
        if (add) {
            return instance.add(value) >= required;
        } else {
            return instance.deduct(value) >= required;
        }
    }

    @Override
    public CriteriaInstance<ItemStack, Inventory> instanceFromJson(JsonObject json) {
        IntegerInstance<ItemStack, Inventory> instance = new IntegerInstance<>(this);
        if (json != null && json.has("count")) {
            instance.add(json.get("count").getAsInt());
        }

        return instance;
    }

    public static Inventory create(@NonNull JsonObject json, @NonNull JsonDeserializationContext context) {
        Inventory.InventoryBuilder<?, ?> builder = builder();

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

        if (json.has("player")) {
            builder.player(context.deserialize(json.get("player"), Player.class));
        }

        return builder.build();
    }
}
