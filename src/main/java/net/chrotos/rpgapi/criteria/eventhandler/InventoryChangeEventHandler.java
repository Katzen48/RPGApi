package net.chrotos.rpgapi.criteria.eventhandler;

import lombok.NonNull;
import net.chrotos.rpgapi.RPGPlugin;
import net.chrotos.rpgapi.criteria.Inventory;
import net.chrotos.rpgapi.quests.Quest;
import net.chrotos.rpgapi.quests.QuestStep;
import net.chrotos.rpgapi.subjects.QuestSubject;
import org.bukkit.Bukkit;
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

public class InventoryChangeEventHandler implements Listener {
    private final RPGPlugin plugin;
    private final HashSet<Quest> relevantQuests = new HashSet<>();
    private final HashSet<QuestStep> relevantQuestSteps = new HashSet<>();
    private final HashSet<QuestCriterion> relevantQuestCriteria = new HashSet<>();

    public InventoryChangeEventHandler(@NonNull RPGPlugin plugin) {
        this.plugin = plugin;
        collectRelevantQuestObjects();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        checkCompletance(event.getEntity(), event.getItem().getItemStack());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onServerCommand(ServerCommandEvent event) {
        if (!event.getCommand().contains("give ")) {
            return;
        }

        onCommand(event.getSender(), event.getCommand());
    }

    private void onCommand(CommandSender sender, String command) {
        if (!(sender instanceof BlockCommandSender blockCommandSender)) {
            return;
        }

        parseTargetPlayers(blockCommandSender, command).forEach(player -> {
            QuestSubject subject = plugin.getQuestManager().getQuestSubject(player.getUniqueId());
            if (!hasActiveInventoryQuestCriterion(subject)) {
                return;
            }

            final int[] hashCodes = new int[player.getInventory().getStorageContents().length];

            for (int i = 0; i < hashCodes.length; i++) {
                hashCodes[i] = Objects.hashCode(player.getInventory().getStorageContents()[i]);
            }

            new BukkitRunnable() {
                @Override
                public void run() {
                    final List<ItemStack> itemStacks = new ArrayList<>();
                    for (int i = 0; i < hashCodes.length; i++) {
                        ItemStack item = player.getInventory().getStorageContents()[i];

                        int hashCode = Objects.hashCode(item);
                        if ((hashCode == hashCodes[i]) || item == null) {
                            continue;
                        }

                        if (itemStacks.stream().anyMatch(itemStack -> itemStack.getType() == item.getType())) {
                            continue;
                        }

                        ItemStack itemStack = item.asOne();
                        itemStack.setType(item.getType());
                        itemStacks.add(itemStack);
                    }

                    if (!itemStacks.isEmpty()) {
                        for (int i = 0; i < hashCodes.length; i++) {
                            ItemStack item = player.getInventory().getStorageContents()[i];
                            if (item == null) {
                                continue;
                            }

                            Optional<ItemStack> itemStack = itemStacks.stream()
                                    .filter(itemStack1 -> itemStack1.getType() == item.getType()).findFirst();

                            itemStack.ifPresent(stack -> stack.setAmount(item.getAmount() + stack.getAmount()));
                        }

                        itemStacks.forEach(itemStack -> checkCompletance(player, itemStack.subtract()));
                    }
                }
            }.runTaskLater(plugin, 0L);
        });
    }

    private Set<Player> parseTargetPlayers(BlockCommandSender blockCommandSender, String command) {
        Set<Player> players = new HashSet<>();
        String giveToSubstring = parseSelector(command.substring(command.indexOf("give ") + "give ".length()).trim());

        if (!giveToSubstring.startsWith("@")) {
            Player player = Bukkit.getPlayer(giveToSubstring);

            if (player != null) {
                players.add(player);
            }
        } else {
            parseSelectorSources(blockCommandSender, command).forEach(selectorSource -> {
                Bukkit.selectEntities(selectorSource, giveToSubstring).forEach(entity -> {
                    if (!(entity instanceof Player player)) {
                        return;
                    }

                    players.add(player);
                });
            });
        }

        return players;
    }

    private List<CommandSender> parseSelectorSources(BlockCommandSender blockCommandSender, String command) {
        ArrayList<CommandSender> entities = new ArrayList<>();

        if (command.contains("execute ")) {
            if (command.contains(" as ")) {
                String selector = parseSelector(command.substring(command.indexOf(" as ") + " as ".length()));

                if (selector.startsWith("@")) {
                    entities.addAll(Bukkit.selectEntities(blockCommandSender, selector));
                }
            } else {
                entities.add(blockCommandSender);
            }
        } else {
            entities.add(blockCommandSender);
        }

        return entities;
    }

    private String parseSelector(String substring) {
        if (!substring.startsWith("@")) {
            return substring.substring(0, substring.indexOf(' '));
        } else {
            if (substring.charAt(2) == '[') {
                return substring.substring(0, substring.indexOf("] ") + 1);
            } else {
                return substring.substring(0, substring.indexOf(' '));
            }
        }
    }

    private boolean hasActiveInventoryQuestCriterion(QuestSubject subject) {
        if (subject == null) {
            return false;
        }

        if (subject.getActiveQuests().isEmpty()) {
            return false;
        }

        return subject.getQuestProgress().stream()
            // Check if quest is relevant
            .anyMatch(progress -> relevantQuests.contains(progress.getQuest()) &&
                progress.getActiveQuestSteps().stream()
                    // Check if active quest steps are relevant
                    .anyMatch(questStep -> relevantQuestSteps.contains(questStep)
                        && questStep.getCriteria().stream()
                        // Check if quest criterion is relevant
                        .anyMatch(questCriterion -> relevantQuestCriteria.contains(questCriterion)
                            // Check if an inventory criterion is defined
                            && questCriterion.getInventory() != null
                            // Check if quest criterion is completed
                            && !progress.getCompletedQuestCriteria().contains(questCriterion)
                            // Check if inventory criterion is completed
                            && !progress.getCompletedCriteria().contains(questCriterion.getInventory()))));
    }

    private void collectRelevantQuestObjects() {
        plugin.getQuestManager().getQuestGraph().getLevels().forEach(level -> {
            level.getQuests().forEach(quest -> {
                quest.getSteps().forEach(step -> {
                    step.getCriteria().forEach(questCriterion -> {
                        if (questCriterion.getInventory() != null) {
                            relevantQuestCriteria.add(questCriterion);
                            relevantQuestSteps.add(step);
                            relevantQuests.add(quest);
                        }
                    });
                });
            });
        });
    }

    private void checkCompletance(Entity entity, ItemStack itemStack) {
        if (!(entity instanceof Player)) {
            return;
        }

        QuestSubject subject = plugin.getQuestManager().getQuestSubject(entity.getUniqueId());

        if (subject != null) {
            subject.trigger(Inventory.TYPE, Inventory.class, itemStack);
        }
    }
}
