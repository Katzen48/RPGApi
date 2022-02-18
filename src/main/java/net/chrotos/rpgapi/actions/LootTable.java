package net.chrotos.rpgapi.actions;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.NamespacedKey;

@Getter
@Builder
public class LootTable implements Actionable {
    /**
     * The namespaced key, of the loot table to be applied.
     */
    @NonNull
    private final NamespacedKey key;
    /**
     * The looting modifier, to be applied to the loot context. If set, overrides the default.
     */
    private final Integer lootingModifier;
}
