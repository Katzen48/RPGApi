package net.chrotos.rpgapi.criteria;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import net.chrotos.rpgapi.selectors.LocationParameters;

@Getter
@SuperBuilder
public class Location extends Criterion {
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
