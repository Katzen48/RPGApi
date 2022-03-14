package net.chrotos.rpgapi.selectors;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Getter
@Builder
public class Player {
    /**
     * The unique id of this player
     */
    private final String id;

    /**
     * The name of this player
     */
    private final String name;

    /**
     * The location, this player is on.
     */
    private final Location location;

    public boolean applies(@NonNull org.bukkit.entity.Player player) {
        return (id == null || id.equals(player.getUniqueId().toString())) &&
                (name == null || name.equals(player.getName())) &&
                (location == null || location.applies(player.getLocation()));
    }
}
