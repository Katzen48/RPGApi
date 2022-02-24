package net.chrotos.rpgapi.quests;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class QuestLevel {
    private final int level;
    private final List<Quest> quests;
    @Setter(AccessLevel.PACKAGE)
    private QuestLevel previousLevel;
    @Setter(AccessLevel.PACKAGE)
    private QuestLevel nextLevel;
}
