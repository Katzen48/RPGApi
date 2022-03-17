package net.chrotos.rpgapi.actions.initialization;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import net.chrotos.rpgapi.actions.*;

@Getter
@SuperBuilder
public class InitializationActions extends Actions {
    /**
     * If the actions should only be executed on activation
     */
    private final boolean once;
}
