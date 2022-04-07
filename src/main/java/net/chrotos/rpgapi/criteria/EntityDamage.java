package net.chrotos.rpgapi.criteria;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import net.chrotos.rpgapi.subjects.QuestSubject;
import org.bukkit.event.entity.EntityDamageEvent;

@Getter
@SuperBuilder
public class EntityDamage extends EntityCriterion implements Checkable<EntityDamageEvent> {
    /**
     * The damage, to be dealt at the entity. If not set, will be one.
     */
    @Builder.Default
    private final Integer damage = 1;

    @Override
    public boolean check(@NonNull QuestSubject subject, @NonNull EntityDamageEvent object) {
        if (!super.check(subject, object.getEntity())) {
            return false;
        }

        return checkIntegerProgress(subject, damage, (int) object.getFinalDamage());
    }
}
