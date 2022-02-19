package net.chrotos.rpgapi.criteria;

import lombok.Setter;
import net.chrotos.rpgapi.quests.QuestCriterion;

@Setter
public class Criterion {
    /**
     * The quest criterion, this criterion is part of.
     */
    private QuestCriterion questCriterion;
}
