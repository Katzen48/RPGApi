package net.chrotos.rpgapi.criteria.eventhandler;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.chrotos.rpgapi.criteria.AdvancementDone;
import net.chrotos.rpgapi.manager.QuestManager;
import net.chrotos.rpgapi.subjects.QuestSubject;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

@RequiredArgsConstructor
public class AdvancementEventHandler implements Listener {
    @NonNull
    private final QuestManager questManager;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerAdvancementDone(PlayerAdvancementDoneEvent event) {
        QuestSubject subject = questManager.getQuestSubject(event.getPlayer().getUniqueId());

        if (subject != null) {
            subject.trigger(AdvancementDone.TYPE, AdvancementDone.class, event.getAdvancement());
        }
    }
}
