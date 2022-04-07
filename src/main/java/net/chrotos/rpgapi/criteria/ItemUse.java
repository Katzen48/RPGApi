package net.chrotos.rpgapi.criteria;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import net.chrotos.rpgapi.subjects.QuestSubject;
import org.bukkit.inventory.ItemStack;

@Getter
@SuperBuilder
public class ItemUse extends ItemCriterion {
    /**
     * The count, of item usages. If not set, will be one.
     */
    @Builder.Default
    private final Integer count = 1;

    @Override
    public boolean check(@NonNull QuestSubject subject, @NonNull ItemStack object) {
        if (!super.check(subject, object)) {
            return false;
        }

        return checkIntegerProgress(subject, count, object.getAmount());
    }
}
