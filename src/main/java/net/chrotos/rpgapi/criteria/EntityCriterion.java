package net.chrotos.rpgapi.criteria;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import net.chrotos.rpgapi.selectors.Location;
import net.chrotos.rpgapi.subjects.QuestSubject;
import org.bukkit.entity.Entity;
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

    public boolean check(@NonNull QuestSubject subject, @NonNull Entity object) {
        return  (id == null || id.equals(object.getUniqueId().toString())) &&
                (type == null || object.getType() == type) &&
                (displayName == null ||
                        (object.customName() != null && getComponentAsPlain(object.customName()).equals(displayName))) &&
                (location == null || location.equals(object.getLocation()));
    }
}
