package net.chrotos.rpgapi.selectors;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Getter
@Builder
public class Location {
    /**
     * The name of the world. Defaults to "world"
     */
    @Builder.Default
    private final String world = "world";
    /**
     * The exact location (Enforced, when set)
     */
    private final LocationParameters exact;
    /**
     * The min location (ignored when "exact" is set; additive to max)
     */
    private final LocationParameters min;
    /**
     * The max location (ignored when "exact" is set; additive to min)
     */
    private final LocationParameters max;

    @Override
    public boolean equals(Object object) {
        return super.equals(object) || (object instanceof org.bukkit.Location && applies((org.bukkit.Location) object));
    }

    public boolean applies(@NonNull org.bukkit.Location location) {
        if (world != null && !location.getWorld().getName().equals(world)) {
            return false;
        }

        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        if (exact != null) {
            return exact.equal(x, y, z);
        }

        return LocationParameters.between(min, max, x, y, z);
    }
}
