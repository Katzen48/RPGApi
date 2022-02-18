package net.chrotos.rpgapi.subjects;

import net.chrotos.rpgapi.actions.*;
import net.chrotos.rpgapi.quests.QuestLevel;

public interface QuestSubject {
    QuestLevel getLevel();
    void award(Advancement advancement);
    void award(Experience experience);
    void award(Loot loot);
    void award(LootTable lootTable);
    void award(Title title);
}
