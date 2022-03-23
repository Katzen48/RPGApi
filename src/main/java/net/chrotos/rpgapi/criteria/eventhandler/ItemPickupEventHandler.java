package net.chrotos.rpgapi.criteria.eventhandler;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.chrotos.rpgapi.criteria.ItemPickup;
import net.chrotos.rpgapi.manager.QuestManager;
import net.chrotos.rpgapi.subjects.QuestSubject;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

@RequiredArgsConstructor
public class ItemPickupEventHandler implements Listener {
    @NonNull
    private final QuestManager questManager;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        QuestSubject subject = questManager.getQuestSubject(event.getEntity().getUniqueId());

        if (subject != null) {
            questManager.checkCompletance(subject, ItemPickup.class, event.getItem().getItemStack());
        }
    }
}
