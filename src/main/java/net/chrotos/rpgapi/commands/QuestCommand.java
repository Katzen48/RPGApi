package net.chrotos.rpgapi.commands;

import com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.chrotos.rpgapi.RPGPlugin;
import net.chrotos.rpgapi.criteria.Checkable;
import net.chrotos.rpgapi.criteria.Criterion;
import net.chrotos.rpgapi.quests.Quest;
import net.chrotos.rpgapi.subjects.QuestSubject;
import net.chrotos.rpgapi.utils.QuestUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

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

        ChestGui gui;
        if (subject.getActiveQuests().size() > 0) {
            Quest quest = subject.getActiveQuests().get(0);
            Component title = deserializeText(subject.getActiveQuests().get(0).getTitle());
            gui = new ChestGui(6, ComponentHolder.of(title));

            List<GuiItem> criteriaItems = collectItems(subject, quest);

            Pane questPane = getQuestPane(subject, quest);
            PaginatedPane criteriaPane = getCriteriaPane(subject, criteriaItems);

            gui.addPane(questPane);
            gui.addPane(criteriaPane);

            if (criteriaItems.size() > (9 * 5)) {
                Pane buttonsPane = getButtonsPane(subject, criteriaPane, gui);
                gui.addPane(buttonsPane);
            }
        } else {
            Component title = Component.translatable("quest.no_quest");
            gui = new ChestGui(1, ComponentHolder.of(title));
            StaticPane questPane = new StaticPane(4, 0, 9, 1);
            questPane.setOnClick(inventoryClickEvent -> inventoryClickEvent.setCancelled(true));

            questPane.addItem(getQuestItem(null, subject.getLocale()), 0, 0);
            gui.addPane(questPane);
        }

        gui.setOnGlobalClick(clickEvent -> {
            if (clickEvent.getCursor() != null) {
                clickEvent.setCancelled(true);
            }
        });
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
                meta.displayName(deserializeText(quest.getTitle()));

                if (quest.getSubTitle() != null) {
                    meta.lore(Collections.singletonList(deserializeText(quest.getSubTitle())));
                }
            }

            itemStack.setItemMeta(meta);
        }

        return new GuiItem(itemStack);
    }

    private Pane getButtonsPane(@NonNull QuestSubject subject, @NonNull PaginatedPane paginatedPane, @NonNull ChestGui gui) {
        StaticPane pane = new StaticPane(9, 1);

        GuiItem prevButton = getSkullWithTranslatableName("MHF_ArrowLeft", "quest.gui.prev_page", subject.getLocale());
        GuiItem nextButton = getSkullWithTranslatableName("MHF_ArrowRight", "quest.gui.next_page", subject.getLocale());

        prevButton.setVisible(false);
        prevButton.setAction(inventoryClickEvent -> {
            inventoryClickEvent.setCancelled(true);

            if (paginatedPane.getPage() < 1) {
                return;
            }

            paginatedPane.setPage(paginatedPane.getPage() - 1);
            nextButton.setVisible(true);

            if (paginatedPane.getPage() < 1) {
                prevButton.setVisible(false);
            }

            gui.update();
        });

        nextButton.setVisible(true);
        nextButton.setAction(inventoryClickEvent -> {
            inventoryClickEvent.setCancelled(true);

            if (paginatedPane.getPage() >= (paginatedPane.getPages() - 1)) {
                return;
            }

            paginatedPane.setPage(paginatedPane.getPage() + 1);
            prevButton.setVisible(true);

            if (paginatedPane.getPage() >= (paginatedPane.getPages() - 1)) {
                nextButton.setVisible(false);
            }

            gui.update();
        });

        pane.addItem(prevButton, 0, 0);
        pane.addItem(nextButton, 8, 0);

        return pane;
    }

    private GuiItem getSkullWithTranslatableName(@NonNull String skullOwner, @NonNull String translationKey, @NonNull Locale locale) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();

        meta.displayName(Component.text(GlobalTranslator.translator().translate(translationKey, locale).format(null)));
        meta.setOwner(skullOwner);
        item.setItemMeta(meta);

        return new GuiItem(item);
    }

    private Pane getQuestPane(@NonNull QuestSubject subject, @NonNull Quest activeQuest) {
        StaticPane pane = new StaticPane(9, 1);
        pane.addItem(getQuestItem(activeQuest, subject.getLocale()), 4, 0);
        pane.setOnClick(inventoryClickEvent -> inventoryClickEvent.setCancelled(true));

        return pane;
    }

    private PaginatedPane getCriteriaPane(@NonNull QuestSubject subject, @NonNull List<GuiItem> criteria) {
        PaginatedPane pane = new PaginatedPane(0, 1, 9, 5);
        pane.populateWithGuiItems(criteria);
        pane.setOnClick(inventoryClickEvent -> inventoryClickEvent.setCancelled(true));

        return pane;
    }

    private <E extends Criterion & Checkable<?>> GuiItem generateItem(@NonNull QuestSubject subject, @NonNull E criterion) {
        return new GuiItem(criterion.getGuiItemStack(subject.getLocale()));
    }

    private <E extends Criterion & Checkable<?>> List<GuiItem> collectItems(@NonNull QuestSubject subject, @NonNull Quest activeQuest) {
        List<GuiItem> items = new ArrayList<>();

        subject.getQuestProgress().stream()
                .filter(questProgress -> questProgress.getQuest() == activeQuest).findFirst().ifPresent(
                        progress -> progress.getActiveQuestSteps().forEach(questStep ->
                        questStep.getCriteria().forEach(questCriterion -> QuestUtil.getCriteria(questCriterion)
                                .forEach(field -> {
                                    try {
                                        field.setAccessible(true);
                                        E criterion = (E) field.get(questCriterion);
                                        if (criterion != null && !progress.getCompletedCriteria().contains(criterion)
                                                && !progress.getCompletedQuestCriteria().contains(criterion.getQuestCriterion())) {
                                            GuiItem item = generateItem(subject, criterion);

                                            if (item.getItem().getType() != Material.AIR) {
                                                items.add(item);
                                            }
                                        }
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    }
                                }))));

        return items;
    }

    private Component deserializeText(@NonNull String text) {
        LegacyComponentSerializer serializer = LegacyComponentSerializer.builder().build();

        return serializer.deserialize(text);
    }
}
