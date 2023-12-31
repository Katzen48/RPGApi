package net.chrotos.rpgapi.criteria;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import net.chrotos.rpgapi.RPGPlugin;
import net.chrotos.rpgapi.criteria.instances.IntegerInstance;
import net.chrotos.rpgapi.subjects.QuestSubject;
import org.bukkit.NamespacedKey;
import org.bukkit.block.data.BlockData;

import java.util.List;

@Getter
@Builder
public class BlockPlacement extends SimpleCriteria<BlockData, BlockPlacement> {
    public static final NamespacedKey TYPE = new NamespacedKey(RPGPlugin.DEFAULT_NAMESPACE, "block_place");

    @Singular("blockdata")
    private final List<BlockData> blockData;

    @Builder.Default
    private final Integer count = 1;

    public boolean check(@NonNull QuestSubject subject, @NonNull BlockData block, @NonNull IntegerInstance<BlockData, BlockPlacement> instance) {
        for (BlockData blockData : this.blockData) {
            if (blockData.matches(block)) {
                return checkIntegerProgress(subject, count, instance);
            }
        }

        return false;
    }

    protected boolean checkIntegerProgress(@NonNull QuestSubject subject, int required, @NonNull IntegerInstance<BlockData, BlockPlacement> instance) {
        return checkIntegerProgress(subject, required, 1, instance);
    }

    protected boolean checkIntegerProgress(@NonNull QuestSubject subject, int required, int value, @NonNull IntegerInstance<BlockData, BlockPlacement> instance) {
        return checkIntegerProgress(subject, required, value, true, instance);
    }

    protected boolean checkIntegerProgress(@NonNull QuestSubject subject, int required, int value, boolean add, @NonNull IntegerInstance<BlockData, BlockPlacement> instance) {
        if (add) {
            return instance.add(value) >= required;
        } else {
            return instance.deduct(value) >= required;
        }
    }

    @Override
    public CriteriaInstance<BlockData, BlockPlacement> instanceFromJson(JsonObject json) {
        IntegerInstance<BlockData, BlockPlacement> instance = new IntegerInstance<>(this);
        if (json != null && json.has("count")) {
            instance.add(json.get("count").getAsInt());
        }

        return instance;
    }

    @Override
    public void trigger(@NonNull QuestSubject subject, @NonNull BlockData value, @NonNull CriteriaInstance<BlockData, BlockPlacement> instance) {
        if (instance instanceof IntegerInstance<BlockData, BlockPlacement> integerInstance) {
            if (check(subject, value, integerInstance)) {
                this.completed = true;
            }
        }
    }

    public static BlockPlacement create(@NonNull JsonObject json, @NonNull JsonDeserializationContext context) {
        BlockPlacement.BlockPlacementBuilder builder = builder();

        builder.blockdata(context.deserialize(json.getAsJsonArray("block_data"), BlockData.class));

        if (json.has("count")) {
            builder.count(json.get("count").getAsInt());
        }

        return builder.build();
    }
}
