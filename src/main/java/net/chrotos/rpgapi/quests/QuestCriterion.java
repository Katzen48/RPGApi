package net.chrotos.rpgapi.quests;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import net.chrotos.rpgapi.criteria.*;

/**
 * All Criteria have to be fulfilled, for the criterion to be fulfilled
 */
@Getter
@Builder
public class QuestCriterion {
    /**
     * The quest step, this criterion belongs to.
     */
    private QuestStep questStep;
    /**
     * The quest, that has to be achieved, to fulfill this criterion. Will be checked upon quest activation.
     */
    private final net.chrotos.rpgapi.criteria.Quest quest;
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
     * The blocks, to be broken.
     */
    private final BlockBreak blockBreak;
    /**
     * The entity damage, to be done.
     */
    private final EntityDamage entityDamage;
    /**
     * The advancement, to be completed. Will be checked upon quest activation.
     */
    private final AdvancementDone advancementDone;
    /**
     * The item, that has to be added to the inventory
     */
    private final Inventory inventory;

    protected void setQuestStep(@NonNull QuestStep questStep) {
        assert this.questStep == null;

        this.questStep = questStep;

        setThisInstance(quest);
        setThisInstance(entityKill);
        setThisInstance(location);
        setThisInstance(itemPickup);
        setThisInstance(itemUse);
        setThisInstance(blockPlacement);
        setThisInstance(blockBreak);
        setThisInstance(entityDamage);
        setThisInstance(advancementDone);
        setThisInstance(inventory);
    }

    private void setThisInstance(Criterion criterion) {
        if (criterion == null) {
            return;
        }

        criterion.setQuestCriterion(this);
    }
}
