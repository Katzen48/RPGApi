package net.chrotos.rpgapi.criteria;

import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import net.chrotos.rpgapi.subjects.QuestSubject;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;

import java.util.List;

@Getter
@SuperBuilder
public class AdvancementDone extends Criterion implements Checkable<Advancement> {
    /**
     * The keys, of the advancements to be done. All are substitutes.
     */
    @Singular("key")
    private final List<NamespacedKey> keys;

    @Override
    public boolean check(@NonNull QuestSubject subject, Advancement advancement) {
        if (advancement != null) {
            return keys.contains(advancement.getKey());
        }

        return keys.stream().anyMatch(
                key -> Bukkit.getPlayer(subject.getUniqueId()).getAdvancementProgress(Bukkit.getAdvancement(key)).isDone());
    }
}
