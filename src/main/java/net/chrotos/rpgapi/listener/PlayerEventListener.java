package net.chrotos.rpgapi.listener;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.chrotos.rpgapi.RPGPlugin;
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
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getQuestManager().onPlayerQuit(event);
    }
}
