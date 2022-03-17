package net.chrotos.rpgapi.criteria;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import net.chrotos.rpgapi.selectors.Player;
import net.chrotos.rpgapi.subjects.QuestSubject;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

@Getter
@SuperBuilder
public class Inventory extends ItemCriterion {
    /**
     * The count, how many items need to be added to the inventory
     */
    @Builder.Default
    private final Integer count = 1;

    /**
     * The player properties, that need to be fulfilled
     */
    private final Player player;

    @Override
    public boolean check(@NonNull QuestSubject subject, @NonNull ItemStack object) {
        if (!super.check(subject, object)) {
            return false;
        }

        if (player != null && !player.applies(Bukkit.getPlayer(subject.getUniqueId()))) {
            return false;
        }

        return checkIntegerProgress(subject, count, object.getAmount(), false);
    }
}
