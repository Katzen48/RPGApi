package net.chrotos.rpgapi.quests;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class QuestGraph {
    private final List<QuestLevel> levels;
}
