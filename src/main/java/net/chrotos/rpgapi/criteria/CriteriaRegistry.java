package net.chrotos.rpgapi.criteria;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import org.bukkit.NamespacedKey;

import java.util.IdentityHashMap;
import java.util.function.BiFunction;

public class CriteriaRegistry {
    private static final IdentityHashMap<NamespacedKey, BiFunction<JsonObject, JsonDeserializationContext, Criteria<?, ?>>> CRITERIA = new IdentityHashMap<>();

    public static void register(NamespacedKey key, BiFunction<JsonObject, JsonDeserializationContext, Criteria<?, ?>> criteria) {
        if (CRITERIA.putIfAbsent(key, criteria) != null) {
            throw new IllegalArgumentException("Criteria with key " + key + " already exists!");
        }
    }

    public static BiFunction<JsonObject, JsonDeserializationContext, Criteria<?, ?>> get(NamespacedKey key) {
        return CRITERIA.get(key);
    }

    static {
        register(AdvancementDone.TYPE, AdvancementDone::create);
        register(BlockBreak.TYPE, BlockBreak::create);
        register(BlockHarvest.TYPE, BlockHarvest::create);
        register(BlockPlacement.TYPE, BlockPlacement::create);
        register(EntityDamage.TYPE, EntityDamage::create);
        register(EntityKill.TYPE, EntityKill::create);
        register(Inventory.TYPE, Inventory::create);
        register(ItemPickup.TYPE, ItemPickup::create);
        register(ItemUse.TYPE, ItemUse::create);
        register(Location.TYPE, Location::create);
        register(NPCInteract.TYPE, NPCInteract::create);
        register(Quest.TYPE, Quest::create);
    }
}
