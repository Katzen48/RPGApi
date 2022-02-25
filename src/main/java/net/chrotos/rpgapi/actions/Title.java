package net.chrotos.rpgapi.actions;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Getter
@Builder
public class Title {
    /**
     * The title, to be shown after completion. Supports translation keys.
     */
    @NonNull
    private final String title;
    /**
     * The subtitle, to be shown after completion. Supports translation keys.
     */
    @NonNull
    private final String subTitle;
}
