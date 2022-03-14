package net.chrotos.rpgapi.criteria.eventhandler;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.chrotos.rpgapi.criteria.Location;
import net.chrotos.rpgapi.manager.QuestManager;
import net.chrotos.rpgapi.subjects.QuestSubject;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

@RequiredArgsConstructor
public class LocationEventHandler implements Listener {
    @NonNull
    private final QuestManager questManager;

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!event.hasChangedBlock()) {
            return;
        }

        QuestSubject subject = questManager.getQuestSubject(event.getPlayer().getUniqueId());

        if (subject != null) {
            questManager.checkCompletance(subject, Location.class, event.getTo());
        }
    }
}
