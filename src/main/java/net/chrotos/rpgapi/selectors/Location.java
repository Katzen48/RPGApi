package net.chrotos.rpgapi.selectors;

import lombok.Builder;
import lombok.Getter;

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
}
