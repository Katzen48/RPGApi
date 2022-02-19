package net.chrotos.rpgapi.criteria;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class Quest extends Criterion {
    /**
     * The id of the quest, that has to be achieved
     */
    @NonNull
    private final String id;
    /**
     * The object instance of the required quest
     */
    private final net.chrotos.rpgapi.quests.Quest quest;
}
