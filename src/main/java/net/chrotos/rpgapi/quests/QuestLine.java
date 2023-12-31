package net.chrotos.rpgapi.quests;

import lombok.Getter;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;

import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Vector;
import java.util.function.Consumer;

@Getter
public class QuestLine {
    private final Vector<Quest> quests;
    private final Component title;
    private final IdentityHashMap<NamespacedKey, Quest> questMap = new IdentityHashMap<>();

    private QuestLine(@NonNull Vector<Quest> quests) {
        this.quests = quests;
        this.title = quests.get(0).getTitle();

        for (Quest quest : quests) {
            questMap.put(quest.getKey(), quest);
        }
    }

    public static QuestLine generate(@NonNull Quest root, @NonNull List<Quest> quests, Consumer<Quest> consumer) {
        LinkedList<NamespacedKey> keys = new LinkedList<>();
        keys.add(root.getKey());

        Vector<Quest> questVector = new Vector<>();
        questVector.add(root);

        int i = 0;
        while (!keys.isEmpty()) {
            Quest quest = quests.get(i);
            if (quest != null) {
                if (!Objects.equals(quest.getKey(), root.getKey()) && quest.getParent() != null) {
                    if (quest.getParent().equals(keys.peek())) {
                        questVector.add(quest);
                        quests.set(i, null);
                        keys.add(quest.getKey());
                    }
                }
            }

            i++;
            if (i >= quests.size()) {
                i = 0;
                keys.removeFirst();
                quests.removeIf(Objects::isNull);
            }
        }

        questVector.forEach(consumer);

        return new QuestLine(questVector);
    }
}
