package net.chrotos.rpgapi.listener;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.chrotos.rpgapi.RPGPlugin;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.EntityTeleportEvent;

@RequiredArgsConstructor
public class NPCEventListener implements Listener {
    @NonNull
    private final RPGPlugin plugin;

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!isNPC(event)) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityTeleport(EntityTeleportEvent event) {
        if (!isNPC(event)) {
            return;
        }

        event.setCancelled(true);
    }

    private boolean isNPC(@NonNull EntityEvent event) {
        if (!(event.getEntity() instanceof Villager)) {
            return false;
        }

        if (plugin.getQuestManager().getNPC((Villager) event.getEntity()) == null) {
            return false;
        }

        return true;
    }
}
