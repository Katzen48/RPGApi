package net.chrotos.rpgapi.commands;

import com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.chrotos.rpgapi.RPGPlugin;
import net.chrotos.rpgapi.criteria.Checkable;
import net.chrotos.rpgapi.criteria.Criterion;
import net.chrotos.rpgapi.quests.Quest;
import net.chrotos.rpgapi.subjects.QuestProgress;
import net.chrotos.rpgapi.subjects.QuestSubject;
import net.chrotos.rpgapi.utils.QuestUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
public class QuestCommand implements CommandExecutor {
    @NonNull
    private final RPGPlugin plugin;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        openGui(player);

        return true;
    }

    private void openGui(@NonNull Player player) {
        QuestSubject subject = plugin.getQuestManager().getQuestSubject(player.getUniqueId());
        if (subject == null) {
            return;
        }

        Component title;
        Pane pane;
        if (subject.getActiveQuests().size() > 0) {
            title = Component.text(subject.getActiveQuests().get(0).getTitle()); //TODO i18n

            pane = getQuestPane(subject, subject.getActiveQuests().get(0));
        } else {
            title = Component.translatable("quest.no_quest");
            StaticPane staticPane = new StaticPane(0, 0, 9, 6);
            pane = staticPane;

            staticPane.addItem(getQuestItem(null, subject.getLocale()), 0, 0);
        }
        ChestGui gui = new ChestGui(6, ComponentHolder.of(title));

        gui.addPane(pane);
        gui.setOnGlobalClick(inventoryClickEvent -> inventoryClickEvent.setCancelled(true));
        gui.show(player);
    }

    private GuiItem getQuestItem(Quest quest, Locale locale) {
        ItemStack itemStack;
        if (quest == null) {
            itemStack = new ItemStack(Material.BARRIER);
            ItemMeta meta = itemStack.getItemMeta();

            meta.displayName(Component.text(GlobalTranslator.translator()
                    .translate("quest.no_quest", locale).format(null)));

            itemStack.setItemMeta(meta);
        } else {
            itemStack = new ItemStack(Material.PAPER);
            ItemMeta meta = itemStack.getItemMeta();

            if (quest.getTitle() != null) {
                meta.displayName(Component.text(quest.getTitle())); //i18n

                if (quest.getSubTitle() != null) {
                    meta.lore(Collections.singletonList(Component.text(quest.getSubTitle())));
                }
            }

            itemStack.setItemMeta(meta);
        }

        return new GuiItem(itemStack);
    }

    private <E extends Criterion & Checkable<?>> Pane getQuestPane(@NonNull QuestSubject subject, @NonNull Quest activeQuest) {
        StaticPane pane = new StaticPane(0, 0, 9, 6);
        QuestProgress progress = subject.getQuestProgress().stream()
                .filter(questProgress -> questProgress.getQuest() == activeQuest).findFirst().orElse(null);

        pane.addItem(getQuestItem(activeQuest, subject.getLocale()), 0, 0);

        AtomicInteger x = new AtomicInteger(2);
        if (progress != null) {
            progress.getActiveQuestSteps().forEach(questStep -> {
                questStep.getCriteria().forEach(questCriterion -> QuestUtil.getCriteria(questCriterion)
                    .forEach(field -> {
                        try {
                            field.setAccessible(true);
                            E criterion = (E) field.get(questCriterion);
                            if (criterion != null) {
                                GuiItem item = generateItem(subject, criterion);
                                if (item.getItem().getType() != Material.AIR) {
                                    pane.addItem(item, x.get() % 9, Math.floorDiv(x.get(), 9));
                                    x.getAndIncrement();
                                }
                            }
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }));
            });
        }

        return pane;
    }

    private <E extends Criterion & Checkable<?>> GuiItem generateItem(@NonNull QuestSubject subject, @NonNull E criterion) {
        return new GuiItem(criterion.getGuiItemStack(subject.getLocale()));
    }
}
