package net.chrotos.rpgapi.quests;

import lombok.Builder;
import lombok.Getter;
import net.chrotos.rpgapi.criteria.*;

/**
 * All Criteria have to be fulfilled, for the criterion to be fulfilled
 */
@Getter
@Builder
public class QuestCriterion {
    /**
     * The quest, that has to be achieved, to fulfill this criterion. Will be checked upon quest activation.
     */
    private final Quest quest;
    /**
     * The entity parameters, that have to be achieved, to fulfill this criterion
     */
    private final EntityKill entityKill;
    /**
     * The location or area, the subject has to move to.
     */
    private final Location location;
    /**
     * The items, to be picked up. Inventory will be checked, upon quest activation.
     */
    private final ItemPickup itemPickup;
    /**
     * The items, to be used.
     */
    private final ItemUse itemUse;
    /**
     * The blocks, to be placed.
     */
    private final BlockPlacement blockPlacement;
    /**
     * The entity damage, to be done.
     */
    private final EntityDamage entityDamage;
    /**
     * The advancement, to be completed. Will be checked upon quest activation.
     */
    private final AdvancementDone advancementDone;
}
