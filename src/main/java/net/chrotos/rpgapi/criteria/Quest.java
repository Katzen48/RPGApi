package net.chrotos.rpgapi.criteria;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
public class Quest extends Criterion {
    /**
     * The id of the quest, that has to be achieved
     */
    @NonNull
    private final String id;
    /**
     * The object instance of the required quest
     */
    @Setter
    private net.chrotos.rpgapi.quests.Quest quest;
}
