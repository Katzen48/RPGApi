package net.chrotos.rpgapi.listener;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.chrotos.rpgapi.RPGPlugin;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@RequiredArgsConstructor
public class PlayerEventListener implements Listener {
    @NonNull
    private final RPGPlugin plugin;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getQuestManager().onPlayerJoin(event);

        Location spawnLocation = event.getPlayer().getBedSpawnLocation();

        if (spawnLocation == null) {
            spawnLocation = event.getPlayer().getWorld().getSpawnLocation();
        }

        if (event.getPlayer().isInsideVehicle()) {
            event.getPlayer().getVehicle().teleport(spawnLocation);
        } else {
            event.getPlayer().teleport(spawnLocation);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getQuestManager().onPlayerQuit(event);
    }
}
