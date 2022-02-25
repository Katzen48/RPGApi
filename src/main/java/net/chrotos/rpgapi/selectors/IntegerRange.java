package net.chrotos.rpgapi.selectors;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.concurrent.ThreadLocalRandom;

@Getter
@SuperBuilder
public class IntegerRange {
    /**
     * The minimum value (additive to max)
     */
    @Builder.Default
    private final Integer min = 1;
    /**
     * The maximum value (additive to min)
     */
    private final Integer max;

    public int getNext() {
        int origin = min != null && min > 0 ? min : 1;
        int bound = max != null && max >= origin ? max : Integer.MAX_VALUE;

        if (bound == Integer.MAX_VALUE) {
            return ThreadLocalRandom.current().nextInt(min);
        }

        return ThreadLocalRandom.current().nextInt(origin, bound + 1);
    }
}
