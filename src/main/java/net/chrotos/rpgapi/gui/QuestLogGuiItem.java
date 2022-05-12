package net.chrotos.rpgapi.gui;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import org.bukkit.Material;

import java.util.List;

@Getter
@Builder
public class QuestLogGuiItem {
    private final Material material;
    private final Translatable displayName;
    @Singular("lore")
    private final List<Translatable> lores;
}
