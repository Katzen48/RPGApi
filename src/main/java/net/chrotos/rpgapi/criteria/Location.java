package net.chrotos.rpgapi.criteria;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import net.chrotos.rpgapi.selectors.LocationParameters;
import net.chrotos.rpgapi.subjects.QuestSubject;

@Getter
@SuperBuilder
public class Location extends Criterion implements Checkable<org.bukkit.Location> {
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
    public boolean check(@NonNull QuestSubject subject, @NonNull org.bukkit.Location object) {
        if (world != null && !object.getWorld().getName().equals(world)) {
            return false;
        }

        int x = object.getBlockX();
        int y = object.getBlockY();
        int z = object.getBlockZ();

        if (exact != null) {
            return exact.equal(x, y, z);
        }

        return LocationParameters.between(min, max, x, y, z);
    }
}
