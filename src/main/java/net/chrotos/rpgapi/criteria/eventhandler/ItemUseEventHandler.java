package net.chrotos.rpgapi.criteria.eventhandler;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.chrotos.rpgapi.criteria.ItemUse;
import net.chrotos.rpgapi.manager.QuestManager;
import net.chrotos.rpgapi.subjects.QuestSubject;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

@RequiredArgsConstructor
public class ItemUseEventHandler implements Listener {
    @NonNull
    private final QuestManager questManager;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick() || !event.hasItem()) {
            return;
        }

        QuestSubject subject = questManager.getQuestSubject(event.getPlayer().getUniqueId());

        if (subject != null) {
            subject.trigger(ItemUse.TYPE, ItemUse.class, event);
        }
    }
}
