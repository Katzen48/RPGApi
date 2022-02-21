package net.chrotos.rpgapi.quests;

import com.google.common.collect.Ordering;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Getter
@AllArgsConstructor
public class QuestGraph {
    private final List<QuestLevel> levels;

    public static QuestGraph generate(@NonNull List<Quest> quests) {
        List<Quest> sortedQuests = Ordering.from(Comparator.comparingInt(Quest::getLevel)).sortedCopy(quests);
        List<QuestLevel> levels = Collections.synchronizedList(new ArrayList<>());

        List<Quest> levelQuests = new ArrayList<>();
        int level = Integer.MIN_VALUE;
        for (Quest quest : sortedQuests) {
            if (level > Integer.MIN_VALUE && level < quest.getLevel()) {
                if (!levelQuests.isEmpty()) {
                    levels.add(new QuestLevel(level, Collections.unmodifiableList(levelQuests)));
                    levelQuests = new ArrayList<>();
                }
            }

            levelQuests.add(quest);
            level = quest.getLevel();
        }

        if (!levelQuests.isEmpty()) {
            levels.add(new QuestLevel(level, Collections.unmodifiableList(levelQuests)));
        }

        return new QuestGraph(Collections.unmodifiableList(levels));
    }
}
