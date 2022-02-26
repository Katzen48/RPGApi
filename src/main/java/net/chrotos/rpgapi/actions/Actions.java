package net.chrotos.rpgapi.actions;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.List;

@Getter
@Builder
public class Actions {
    /**
     * The loot, to be awarded. This is additive.
     */
    @Singular("loot")
    private final List<Loot> loots;
    /**
     * The loot tables, to be applied. This is additive.
     */
    @Singular("lootTable")
    private final List<LootTable> lootTables;
    /**
     * The experience, to be awarded. This is additive. If both values are set, a random amount will be awarded.
     */
    private final Experience experience;
    /**
     * The advancements, to be awarded. This is additive.
     */
    @Singular("advancement")
    private final List<Advancement> advancements;
    /**
     * The title/subtitle, to be shown. This is additive.
     */
    private final Title title;
    /**
     * The commands, that should be executed
     */
    @Singular("command")
    private final List<Command> commands;
}
