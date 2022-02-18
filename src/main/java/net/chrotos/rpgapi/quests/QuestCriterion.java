package net.chrotos.rpgapi.quests;

import lombok.Builder;
import lombok.Getter;
import net.chrotos.rpgapi.criteria.EntityKill;
import net.chrotos.rpgapi.selectors.Location;

@Getter
@Builder
public class QuestCriterion {
    /**
     * The quest, that has to be achieved, to fulfill this criterion
     */
    private final Quest quest;
    /**
     * The entity parameters, that have to be achieved, to fulfill this criterion
     */
    private final EntityKill entityKill;
    /**
     * The location or area, the subject has to move to
     */
    private final Location location;
}
