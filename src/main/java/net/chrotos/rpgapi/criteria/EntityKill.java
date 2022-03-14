package net.chrotos.rpgapi.criteria;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import net.chrotos.rpgapi.subjects.QuestSubject;
import org.bukkit.entity.Entity;

@Getter
@SuperBuilder
public class EntityKill extends EntityCriterion implements Checkable<Entity> {
    /**
     * The count, of entities to be killed (requires type to be set, should not be used with id)
     */
    private final Integer count = 1;

    @Override
    public boolean check(@NonNull QuestSubject subject, @NonNull Entity object) {
        if (!super.check(subject, object)) {
            return false;
        }

        return checkIntegerProgress(subject, count);
    }
}
