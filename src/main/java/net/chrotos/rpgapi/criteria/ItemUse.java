package net.chrotos.rpgapi.criteria;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class ItemUse extends ItemCriterion {
    /**
     * The count, of item usages. If not set, will be one.
     */
    @Builder.Default
    private final Integer count = 1;
}