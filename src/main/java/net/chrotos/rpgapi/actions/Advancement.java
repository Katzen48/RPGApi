package net.chrotos.rpgapi.actions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.NamespacedKey;

@Getter
@AllArgsConstructor
public class Advancement {
    /**
     * The namespaced key, of the advancement to be granted.
     */
    private final NamespacedKey key;
}
