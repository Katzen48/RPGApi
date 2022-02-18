package net.chrotos.rpgapi.quests;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class QuestLevel {
    private final int level;
    private final List<Quest> quests;
    @Setter
    private QuestLevel previousLevel;
    @Setter
    private QuestLevel nextLevel;
}
