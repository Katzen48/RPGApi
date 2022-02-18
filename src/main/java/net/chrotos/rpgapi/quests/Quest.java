package net.chrotos.rpgapi.quests;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import net.chrotos.rpgapi.actions.Actions;

import java.util.List;

@Getter
@Builder
public class Quest {
    /**
     * Unique Identifier of the quest.
     */
    @NonNull
    private final String id;
    /**
     * Name of the quest. Supports translation keys.
     */
    @NonNull
    private final String name;
    /**
     * In which quest tab this quest should be shown
     */
    private final String questTab;
    /**
     * If the quest should stay hidden until completed
     */
    private final boolean hidden;
    /**
     * If the completed of this quest should be announced
     */
    private final boolean announce;
    /**
     * The title, to be shown. Supports translation keys.
     */
    private final String title;
    /**
     * The subtitle, to be shown. Supports translation keys.
     */
    private final String subTitle;
    /**
     * The steps for this quest
     */
    @Singular("step")
    private final List<QuestStep> steps;
    /**
     * The actions, to be executed after quest completion.
     */
    private final Actions actions;
}
