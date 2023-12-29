package net.chrotos.rpgapi.actions;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import net.kyori.adventure.text.Component;

@Getter
@Builder
public class Title {
    /**
     * The title, to be shown after completion. Supports translation keys.
     */
    @NonNull
    private final Component title;
    /**
     * The subtitle, to be shown after completion. Supports translation keys.
     */
    @NonNull
    private final Component subTitle;
}
