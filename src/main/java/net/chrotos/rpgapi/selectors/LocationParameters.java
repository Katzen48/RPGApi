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
}
