package net.chrotos.rpgapi.criteria;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import net.chrotos.rpgapi.selectors.Location;
import org.bukkit.entity.EntityType;

@Getter
@SuperBuilder
public abstract class EntityCriterion extends Criterion {
    /**
     * The exact entity id, of the entity
     */
    private final String id;
    /**
     * The type, of the entity
     */
    private final EntityType type;
    /**
     * The Display Name, of the entity
     */
    private final String displayName;
    /**
     * The location selector, of the entity
     */
    private final Location location;
}
