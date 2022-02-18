package net.chrotos.rpgapi.criteria;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import org.bukkit.NamespacedKey;

import java.util.List;

@Getter
@Builder
public class AdvancementDone {
    /**
     * The keys, of the advancements to be done. All are substitutes.
     */
    @Singular("key")
    private final List<NamespacedKey> keys;
}
