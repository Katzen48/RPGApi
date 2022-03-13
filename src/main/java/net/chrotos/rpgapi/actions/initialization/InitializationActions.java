package net.chrotos.rpgapi.actions.initialization;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import net.chrotos.rpgapi.actions.*;

import java.util.List;

@Getter
@SuperBuilder
public class InitializationActions extends Actions {
    /**
     * If the actions should only be executed on activation
     */
    private final boolean once;
}
