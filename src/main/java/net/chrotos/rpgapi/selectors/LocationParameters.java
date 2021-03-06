package net.chrotos.rpgapi.selectors;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LocationParameters {
    /**
     * The value for x
     */
    private final Integer x;
    /**
     * The value for y
     */
    private final Integer y;
    /**
     * The value for z
     */
    private final Integer z;

    public boolean equal(int x, int y, int z) {
        return ((getX() == null || x == getX()) &&
                (getY() == null || y == getY()) &&
                (getZ() == null || z == getZ()));
    }

    public boolean greaterThan(int x, int y, int z) {
        return ((getX() == null || x <= getX()) &&
                (getY() == null || y <= getY()) &&
                (getZ() == null || z <= getZ()));
    }

    public boolean lessThan(int x, int y, int z) {
        return ((getX() == null || x >= getX()) &&
                (getY() == null || y >= getY()) &&
                (getZ() == null || z >= getZ()));
    }

    public static boolean between(LocationParameters min, LocationParameters max, int x, int y, int z) {
        if (min != null && !min.lessThan(x, y, z)) {
            return false;
        }

        return max == null || max.greaterThan(x, y, z);
    }
}
