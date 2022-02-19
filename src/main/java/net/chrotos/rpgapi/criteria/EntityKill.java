package net.chrotos.rpgapi.criteria;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class EntityKill extends EntityCriterion {
    /**
     * The count, of entities to be killed (requires type to be set, should not be used with id)
     */
    private final Integer count = 1;

}
