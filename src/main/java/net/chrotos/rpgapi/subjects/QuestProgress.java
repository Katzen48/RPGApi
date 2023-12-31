package net.chrotos.rpgapi.subjects;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import lombok.*;
import net.chrotos.rpgapi.criteria.Criteria;
import net.chrotos.rpgapi.criteria.CriteriaInstance;
import net.chrotos.rpgapi.quests.Quest;
import net.chrotos.rpgapi.quests.QuestStep;
import org.bukkit.NamespacedKey;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Data
@Builder
@RequiredArgsConstructor
public class QuestProgress {
    @Setter(AccessLevel.PACKAGE)
    private QuestSubject questSubject;

    @NonNull
    private final List<Quest> activeQuests = Collections.synchronizedList(new CopyOnWriteArrayList<>());

    @NonNull
    private final List<NamespacedKey> completedQuests = Collections.synchronizedList(new CopyOnWriteArrayList<>());

    @Builder.Default
    private final List<QuestStep> activeQuestSteps = Collections.synchronizedList(new CopyOnWriteArrayList<>());

    @Builder.Default
    private final List<QuestStep> completedQuestSteps = Collections.synchronizedList(new CopyOnWriteArrayList<>());

    @Builder.Default
    private final ListMultimap<NamespacedKey, CriteriaInstance<?,?>> criteriaInstances = Multimaps.synchronizedListMultimap(ArrayListMultimap.create());

    public <T, A extends Criteria<T, A>, C extends CriteriaInstance<T,A>> List<C> getCriteriaInstances(NamespacedKey key, Class<A> clazz) {
        return (List<C>) criteriaInstances.get(key);
    }
}
