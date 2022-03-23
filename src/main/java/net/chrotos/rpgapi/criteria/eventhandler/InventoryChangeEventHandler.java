package net.chrotos.rpgapi.criteria.eventhandler;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.chrotos.rpgapi.RPGPlugin;
import net.chrotos.rpgapi.criteria.Inventory;
import net.chrotos.rpgapi.subjects.QuestSubject;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

@RequiredArgsConstructor
public class InventoryChangeEventHandler implements Listener {
    @NonNull
    private final RPGPlugin plugin;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        checkCompletance(event.getEntity(), event.getItem().getItemStack());
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onServerCommand(ServerCommandEvent event) {
        if (!event.getCommand().contains("give ")) {
            return;
        }

        else
        onCommand(event.getSender());
    }

    private void onCommand(CommandSender sender) {
        if (!(sender instanceof BlockCommandSender)) {
            return;
        }
        BlockCommandSender blockCommandSender = (BlockCommandSender) sender;

        blockCommandSender.getBlock().getWorld().getPlayers().forEach(player -> {
            final int[] hashCodes = new int[player.getInventory().getStorageContents().length];

            for (int i = 0; i < hashCodes.length; i++) {
                hashCodes[i] = Objects.hashCode(player.getInventory().getStorageContents()[i]);
            }

            new BukkitRunnable() {
                @Override
                public void run() {
                    ItemStack itemStack = null;
                    for (int i = 0; i < hashCodes.length; i++) {
                        ItemStack item = player.getInventory().getStorageContents()[i];

                        int hashCode = Objects.hashCode(item);

                        if ((hashCode == hashCodes[i] && itemStack == null) || item == null) {
                            continue;
                        }

                        if (itemStack == null) {
                            itemStack = item.clone();
                            continue;
                        }

                        if (itemStack.getType() == item.getType()) {
                            itemStack.add(item.getAmount());
                        }
                    }

                    if (itemStack != null) {
                        checkCompletance(player, itemStack);
                    }
                }
            }.runTaskLater(plugin, 0L);
        });
    }

    private void checkCompletance(Entity entity, ItemStack itemStack) {
        if (!(entity instanceof Player)) {
            return;
        }

        QuestSubject subject = plugin.getQuestManager().getQuestSubject(entity.getUniqueId());

        if (subject != null) {
            plugin.getQuestManager().checkCompletance(subject, Inventory.class, itemStack);
        }
    }
}
