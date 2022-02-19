package net.chrotos.rpgapi.criteria;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Quest extends Criterion {
    /**
     * The id of the quest, that has to be achieved
     */
    private final String id;
    /**
     * The object instance of the required quest
     */
    private final net.chrotos.rpgapi.quests.Quest quest;
}
