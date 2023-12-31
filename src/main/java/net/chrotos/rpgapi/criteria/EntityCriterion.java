package net.chrotos.rpgapi.criteria;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import net.chrotos.rpgapi.criteria.instances.IntegerInstance;
import net.chrotos.rpgapi.selectors.Location;
import net.chrotos.rpgapi.subjects.QuestSubject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

@Getter
@Builder
public abstract class EntityCriterion<T, A extends EntityCriterion<T, A>> extends SimpleCriteria<T, A> {
    private final String id;
    private final EntityType type;
    private final String displayName;
    private final Location location;

    protected boolean check(@NonNull QuestSubject subject, @NonNull T value, @NonNull IntegerInstance<T, A> instance) {
        Entity object = getEntity(value);
        return  (id == null || id.equals(object.getUniqueId().toString())) &&
                (type == null || object.getType() == type) &&
                (displayName == null ||
                        (object.customName() != null && getComponentAsPlain(object.customName()).equals(displayName))) &&
                (location == null || location.equals(object.getLocation()));
    }

    @Override
    public void trigger(@NonNull QuestSubject subject, @NonNull T value, @NonNull CriteriaInstance<T, A> instance) {
        if (instance instanceof IntegerInstance<T, A> integerInstance) {
            if (check(subject, value, integerInstance)) {
                this.completed = true;
            }
        }
    }

    @NonNull
    abstract Entity getEntity(@NonNull T value);

    protected String getComponentAsPlain(@NonNull Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }
}
