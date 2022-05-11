package net.chrotos.rpgapi.subjects;

import lombok.*;
import net.chrotos.rpgapi.RPGPlugin;
import net.chrotos.rpgapi.actions.*;
import net.chrotos.rpgapi.manager.QuestManager;
import net.chrotos.rpgapi.quests.Quest;
import net.chrotos.rpgapi.quests.QuestLevel;
import net.chrotos.rpgapi.quests.QuestStep;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.loot.LootContext;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

@Getter(onMethod = @__(@Synchronized))
public class DefaultQuestSubject implements QuestSubject {
    @NonNull
    private final UUID uniqueId;
    @Setter(onMethod = @__({@Synchronized, @Override}), onParam = @__(@NonNull))
    private Player player;
    @Setter(onMethod = @__({@Synchronized, @Override}), onParam = @__(@NonNull))
    private QuestLevel level;
    @Setter(onMethod = @__({@Synchronized, @Override}), onParam = @__(@NonNull))
    private List<Quest> completedQuests;
    @Setter(onMethod = @__({@Synchronized, @Override}), onParam = @__(@NonNull))
    private List<Quest> activeQuests;
    @Setter(onMethod = @__({@Synchronized, @Override}), onParam = @__(@NonNull))
    private List<QuestProgress> questProgress;

    protected DefaultQuestSubject(@NonNull UUID uniqueId) {
        this.uniqueId = uniqueId;
    }

    @Override
    public @NonNull Locale getLocale() {
        return player != null ? player.locale() : Locale.US;
    }

    @Override
    @NonNull
    public String getName() {
        return player.getName();
    }

    @Override
    @Deprecated
    @NonNull
    public String getDisplayName() {
        return player.getDisplayName();
    }

    @Override
    @Synchronized
    public void award(@NonNull Advancement... advancements) {
        for (Advancement advancement : advancements) {
            AdvancementProgress progress = player.getAdvancementProgress(Bukkit.getAdvancement(advancement.getKey()));
            for (String criteria : progress.getRemainingCriteria()) {
                progress.awardCriteria(criteria);
            }
        }
    }

    @Override
    @Synchronized
    public void award(@NonNull Experience experience) {
        player.giveExp(experience.getNext());
    }

    @Override
    @Synchronized
    public void award(@NonNull Loot... loots) throws IllegalStateException {
        NamespacedKey key = new NamespacedKey(RPGPlugin.getInstance(), "award");

        for (Loot loot : loots) {
            ItemStack itemStack = new ItemStack(loot.getMaterial(), loot.getCount().getNext());
            itemStack.getItemMeta().getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);

            if (loot.getDurability() != null) {
                itemStack.setDurability(loot.getDurability());
            }

            if (loot.getDisplayName() != null) {
                ItemMeta meta = itemStack.getItemMeta();
                meta.displayName(LegacyComponentSerializer.builder().build().deserialize(loot.getDisplayName())); // TODO: i18n
                itemStack.setItemMeta(meta);
            }

            player.getInventory().addItem(itemStack);
        }
    }

    @Override
    @Synchronized
    public void award(@NonNull LootTable... lootTables) {
        for (LootTable lootTable : lootTables) {
            LootContext.Builder contextBuilder = new LootContext.Builder(player.getLocation()).killer(player);
            if (lootTable.getLootingModifier() != null) {
                contextBuilder.lootingModifier(lootTable.getLootingModifier());
            }

            Bukkit.getLootTable(lootTable.getKey())
                    .populateLoot(new Random(), contextBuilder.build()).forEach(player.getInventory()::addItem);
        }
    }

    @Override
    @Synchronized
    public void award(@NonNull Title title) {
        showTitle(title.getTitle(), title.getSubTitle());
    }

    @Override
    public void award(@NonNull Command command) {
        if (command.asServer()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.format(this));
        } else {
            player.performCommand(command.format(this));
        }
    }

    @Override
    @Synchronized
    public void complete(@NonNull Quest quest) {
        award(quest.getActions());
    }

    @Override
    @Synchronized
    public void complete(@NonNull QuestStep questStep) {
        award(questStep.getActions());
    }

    @Override
    public void activate(@NonNull Quest quest, @NonNull QuestManager questManager) {
        if (getActiveQuests().contains(quest)) {
            return;
        }

        // Find first quest step level
        int level = quest.getSteps().stream().mapToInt(QuestStep::getLevel).min().getAsInt();

        // Get or create progress
        QuestProgress curQuestProgress = getQuestProgress().stream()
                .filter(progress -> progress.getQuest() == quest).findFirst()
                .orElse(null);

        if (curQuestProgress == null) {
            curQuestProgress = QuestProgress.builder().quest(quest).build();
            getQuestProgress().add(curQuestProgress);
        }
        // Add quest step to activeQuestSteps
        QuestProgress finalCurQuestProgress = curQuestProgress;
        quest.getSteps().stream().filter(questStep -> questStep.getLevel() == level)
                .forEach(questStep -> finalCurQuestProgress.getActiveQuestSteps().add(questStep));

        award(quest.getInitializationActions());
        showTitle(quest.getTitle(), quest.getSubTitle());
        getActiveQuests().add(quest);

        // TODO check if required quests have already been completed. Refactoring required
        questManager.checkCompletance(this, net.chrotos.rpgapi.criteria.Quest.class, null);
    }

    public void showTitle(String title, String subTitle) {
        if (title == null) {
            return;
        }

        LegacyComponentSerializer serializer = LegacyComponentSerializer.builder().build();
        Component subTitleComponent;

        if (subTitle == null) {
            subTitleComponent = Component.empty();
        } else {
            subTitleComponent = serializer.deserialize(subTitle);
        }

        player.showTitle(net.kyori.adventure.title.Title.title(serializer.deserialize(title), subTitleComponent)); // TODO i18n
    }

    public static DefaultQuestSubject create(@NonNull UUID uniqueId) {
        return new DefaultQuestSubject(uniqueId);
    }
}
