package net.chrotos.rpgapi.criteria.eventhandler;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.chrotos.rpgapi.criteria.BlockPlacement;
import net.chrotos.rpgapi.manager.QuestManager;
import net.chrotos.rpgapi.subjects.QuestSubject;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

@RequiredArgsConstructor
public class BlockPlacementEventHandler implements Listener {
    @NonNull
    private final QuestManager questManager;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        QuestSubject subject = questManager.getQuestSubject(event.getPlayer().getUniqueId());

        if (subject != null) {
            questManager.checkCompletance(subject, BlockPlacement.class, event.getBlock());
        }
    }
}
