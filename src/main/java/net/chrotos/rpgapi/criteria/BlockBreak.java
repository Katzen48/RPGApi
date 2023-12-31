package net.chrotos.rpgapi.criteria;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import net.chrotos.rpgapi.RPGPlugin;
import net.chrotos.rpgapi.criteria.instances.IntegerInstance;
import net.chrotos.rpgapi.subjects.QuestSubject;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@Getter
@SuperBuilder
public class BlockBreak extends ItemCriterion<BlockBreakEvent, BlockBreak> {
    public static final NamespacedKey TYPE = new NamespacedKey(RPGPlugin.DEFAULT_NAMESPACE, "block_break");

    @Singular("material")
    private final List<Material> blocks;

    public boolean check(@NonNull QuestSubject subject, @NonNull BlockBreakEvent event, @NonNull IntegerInstance<BlockBreakEvent, BlockBreak> instance) {
        if (!super.check(subject, event, instance)) {
            return false;
        }

        for (Material material : this.blocks) {
            if (material == event.getBlock().getType()) {
                return checkIntegerProgress(subject, getCount(), instance);
            }
        }

        return false;
    }

    protected boolean checkIntegerProgress(@NonNull QuestSubject subject, int required, @NonNull IntegerInstance<BlockBreakEvent, BlockBreak> instance) {
        return checkIntegerProgress(subject, required, 1, instance);
    }

    protected boolean checkIntegerProgress(@NonNull QuestSubject subject, int required, int value, @NonNull IntegerInstance<BlockBreakEvent, BlockBreak> instance) {
        return checkIntegerProgress(subject, required, value, true, instance);
    }

    protected boolean checkIntegerProgress(@NonNull QuestSubject subject, int required, int value, boolean add, @NonNull IntegerInstance<BlockBreakEvent, BlockBreak> instance) {
        if (add) {
            return instance.add(value) >= required;
        } else {
            return instance.deduct(value) >= required;
        }
    }

    @Override
    public CriteriaInstance<BlockBreakEvent, BlockBreak> instanceFromJson(JsonObject json) {
        IntegerInstance<BlockBreakEvent, BlockBreak> instance = new IntegerInstance<>(this);
        if (json != null && json.has("count")) {
            instance.add(json.get("count").getAsInt());
        }

        return instance;
    }

    @Override
    public void trigger(@NonNull QuestSubject subject, @NonNull BlockBreakEvent value, @NonNull CriteriaInstance<BlockBreakEvent, BlockBreak> instance) {
        if (instance instanceof IntegerInstance<BlockBreakEvent, BlockBreak> integerInstance) {
            if (check(subject, value, integerInstance)) {
                this.completed = true;
            }
        }
    }

    @Override
    @NonNull ItemStack getItemStack(@NonNull BlockBreakEvent value) {
        return value.getPlayer().getActiveItem();
    }

    public static BlockBreak create(@NonNull JsonObject json, @NonNull JsonDeserializationContext context) {
        BlockBreak.BlockBreakBuilder<?,?> builder = builder();

        if (json.has("materials")) {
            json.get("materials").getAsJsonArray().forEach(element -> builder.material(Material.getMaterial(element.getAsString())));
        }

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
