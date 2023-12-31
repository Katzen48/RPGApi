package net.chrotos.rpgapi.quests;

import io.papermc.paper.advancement.AdvancementDisplay;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.Singular;
import net.chrotos.rpgapi.actions.Actions;
import net.chrotos.rpgapi.actions.initialization.InitializationActions;
import net.chrotos.rpgapi.criteria.Criteria;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;

import java.util.List;

@Getter
@Builder
public class Quest {
    @NonNull
    @Setter
    private NamespacedKey key;

    @NonNull
    private final Component title;

    @NonNull
    private final Component description;

    //private final String npc;

    private final AdvancementDisplay.Frame frame;

    @Builder.Default
    private final boolean hidden = false;

    @Builder.Default
    private final boolean announce = false;

    private final Component subTitle;

    private final NamespacedKey parent;

    @Singular("step")
    private final List<QuestStep> steps;

    private final Actions actions;

    private final InitializationActions initializationActions;

    public Criteria<?,?> getCriteria(@NonNull String id) {
        for (QuestStep step : steps) {
            for (Criteria<?, ?> criterion : step.getCriteria()) {
                if (criterion.getId().equals(id)) {
                    return criterion;
                }
            }
        }

        return null;
    }
}
