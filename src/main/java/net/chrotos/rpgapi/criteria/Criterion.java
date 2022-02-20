package net.chrotos.rpgapi.criteria;

import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.chrotos.rpgapi.quests.QuestCriterion;

@Setter
@SuperBuilder()
@NoArgsConstructor
public class Criterion {
    /**
     * The quest criterion, this criterion is part of.
     */
    private QuestCriterion questCriterion;
}
