package net.chrotos.rpgapi.selectors;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ShortRange {
    /**
     * The minimum value (additive to max)
     */
    private final Short min;
    /**
     * THe maximum value (additive to min)
     */
    private final Integer max;
}
