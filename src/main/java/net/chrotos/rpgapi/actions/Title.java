package net.chrotos.rpgapi.actions;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Title {
    /**
     * The title, to be shown after completion. Supports translation keys.
     */
    private final String title;
    /**
     * The subtitle, to be shown after completion. Supports translation keys.
     */
    private final String subTitle;
}
