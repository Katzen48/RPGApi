package net.chrotos.rpgapi.criteria;

import lombok.Getter;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import org.bukkit.NamespacedKey;

import java.util.List;

@Getter
@SuperBuilder
public class AdvancementDone extends Criterion {
    /**
     * The keys, of the advancements to be done. All are substitutes.
     */
    @Singular("key")
    private final List<NamespacedKey> keys;
}
