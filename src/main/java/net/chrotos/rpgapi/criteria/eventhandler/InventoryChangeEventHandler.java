package net.chrotos.rpgapi.criteria.eventhandler;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.chrotos.rpgapi.RPGPlugin;
import net.chrotos.rpgapi.criteria.Inventory;
import net.chrotos.rpgapi.subjects.QuestSubject;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
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

    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        checkCompletance(event.getEntity(), event.getItem().getItemStack());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onServerCommand(ServerCommandEvent event) {
        if (!event.getCommand().startsWith("give") && !event.getCommand().startsWith("/give")) {
            return;
        }

        onCommand(event.getSender(), event.getCommand());
    }

    private void onCommand(CommandSender sender, String command) {
        int openingBrackets = StringUtils.countMatches(command, "[");
        int closingBrackets = StringUtils.countMatches(command, "]");

        // Check if command has enough spaces (hopefully for arguments) and the selector is closed
        if (StringUtils.countMatches(command, " ") < 4 || openingBrackets != closingBrackets ) {
            return;
        }

        String selector;
        if (openingBrackets > 0) {
            int selectorStart = command.indexOf("@");
            if (selectorStart < 0) {
                return;
            }

            // Find end of the selector
            int brackets = 0;
            int currentIndex = selectorStart;
            do {
                if (command.charAt(currentIndex + 1) == '[') {
                    brackets++;
                } else if (command.charAt(currentIndex + 1) == ']') {
                    brackets--;
                }

                currentIndex++;
            } while (brackets > 0 && currentIndex < command.length() - 2);
            selector = command.substring(selectorStart, currentIndex + 1);

            if (selector.trim().equals("")) {
                return;
            }
        } else {
            String[] commandParts = command.split(" ");
            selector = commandParts[1];
        }

        if (selector == null) {
            return;
        }

        Bukkit.selectEntities(sender, selector).stream().filter(entity -> entity instanceof Player)
                .forEach(entity -> {
                    final Player player = (Player) entity;

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

                                if (hashCode == hashCodes[i] || item == null) {
                                    continue;
                                }

                                if (itemStack == null) {
                                    itemStack = item.clone();
                                    continue;
                                }

                                itemStack.add(item.getAmount());
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
