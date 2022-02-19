package net.chrotos.rpgapi.criteria;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class EntityDamage extends EntityCriterion {
    /**
     * The damage, to be dealt at the entity. If not set, will be one.
     */
    private final Integer damage = 1;
}
