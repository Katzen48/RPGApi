package net.chrotos.rpgapi.criteria;

import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.chrotos.rpgapi.quests.QuestCriterion;

@Setter
@SuperBuilder
public class Criterion {
    /**
     * The quest criterion, this criterion is part of.
     */
    private QuestCriterion questCriterion;
}
