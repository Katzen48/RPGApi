package net.chrotos.rpgapi.listener;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.chrotos.rpgapi.RPGPlugin;
import net.chrotos.rpgapi.npc.NPC;
import net.chrotos.rpgapi.quests.Quest;
import net.chrotos.rpgapi.subjects.QuestSubject;
import net.chrotos.rpgapi.utils.QuestUtil;
import org.bukkit.Location;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

@RequiredArgsConstructor
public class PlayerEventListener implements Listener {
    @NonNull
    private final RPGPlugin plugin;

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractAtEntity(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND || !(event.getRightClicked() instanceof Villager)) {
            return;
        }

        NPC npc = plugin.getQuestManager().getNPC((Villager) event.getRightClicked());

        if (npc == null) {
            return;
        }

        event.setCancelled(true);

        QuestSubject subject = plugin.getQuestManager().getQuestSubject(event.getPlayer().getUniqueId());
        if(subject == null) {
            return;
        }

        Quest nextQuest = npc.getNextQuest(subject);
        if (nextQuest == null) {
            return;
        }

        subject.activate(nextQuest, plugin.getQuestManager());
        plugin.getQuestManager().saveQuestSubject(subject.getUniqueId());
    }

    @EventHandler(ignoreCancelled = true)
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
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick() || !event.hasItem()) {
            return;
        }

        if (!QuestUtil.isQuestBook(event.getItem())) {
            return;
        }

        event.getPlayer().performCommand("rpgapi:quest");
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getQuestManager().onPlayerQuit(event.getPlayer());
    }
}
