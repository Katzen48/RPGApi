package net.chrotos.rpgapi.criteria;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class EntityDamage extends EntityCriterion {
    /**
     * The damage, to be dealt at the entity. If not set, will be one.
     */
    @NonNull
    private final Integer damage;
}
