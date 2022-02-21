package net.chrotos.rpgapi.selectors;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class IntegerRange {
    /**
     * The minimum value (additive to max)
     */
    @Builder.Default
    private final Integer min = 1;
    /**
     * THe maximum value (additive to min)
     */
    private final Integer max;
}
