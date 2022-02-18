package net.chrotos.rpgapi.criteria;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Getter
@Builder
public class EntityKill extends EntityCriterion {
    /**
     * The count, of entities to be killed (requires type to be set, should not be used with id)
     */
    @NonNull
    private final Integer count;
}
